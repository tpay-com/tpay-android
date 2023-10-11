@file:Suppress("unused", "UNCHECKED_CAST", "MemberVisibilityCanBePrivate")

package com.tpay.sdk.di

import java.lang.reflect.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.*

internal class DI private constructor(modules: Iterable<*>) {
    private val providers: MutableMap<Key<*>, Provider<*>> = ConcurrentHashMap()
    private val singletons: MutableMap<Key<*>, Any> = ConcurrentHashMap()
    private val injectFields: MutableMap<Class<*>, Array<Array<Any>>> = ConcurrentHashMap(0)

    /**
     * @return an instance of type
     */
    fun <T> instance(type: Class<T>): T {
        return provider(Key.of(type), null).get()
    }

    /**
     * @return instance specified by key (type and qualifier)
     */
    fun <T> instance(key: Key<T>): T {
        return provider(key, null).get()
    }

    /**
     * @return provider of type
     */
    fun <T> provider(type: Class<T>): Provider<T> {
        return provider(Key.of(type), null)
    }

    /**
     * @return provider of key (type, qualifier)
     */
    fun <T> provider(key: Key<T>): Provider<T> {
        return provider(key, null)
    }

    /**
     * Injects fields to the target object
     */
    fun injectFields(target: Any) {
        if (!injectFields.containsKey(target.javaClass)) {
            injectFields[target.javaClass] = Companion.injectFields(target.javaClass)
        }
        for (f in injectFields[target.javaClass]!!) {
            val field = f[0] as Field
            val key = f[2] as Key<Any>
            try {
                field.set(target, if (f[1] as Boolean) provider(key) else instance(key))
            } catch (e: Exception) {
                throw DIException(
                    String.format(
                        "Can't inject field %s in %s",
                        field.name,
                        target.javaClass.name
                    )
                )
            }
        }
    }

    private fun <T> provider(key: Key<T>, chain: Set<Key<*>>?): Provider<T> {
        if (!providers.containsKey(key)) {
            val constructor = constructor(key)
            val paramProviders = paramProviders(
                key,
                constructor.parameterTypes,
                constructor.genericParameterTypes,
                constructor.parameterAnnotations,
                chain
            )
            providers[key] = singletonProvider<Any>(
                key,
                key.type.getAnnotation(Singleton::class.java),
                Provider {
                    try {
                        return@Provider constructor.newInstance(*params(paramProviders))
                    } catch (e: Exception) {
                        throw DIException(String.format("Can't instantiate %s", key), e)
                    }
                })
        }
        return providers[key] as Provider<T>
    }

    private fun providerMethod(module: Any, m: Method) {
        val key = Key.of(m.returnType, qualifier(m.annotations))
        if (providers.containsKey(key)) {
            throw DIException(
                String.format(
                    "%s has multiple providers, module %s",
                    key,
                    module.javaClass
                )
            )
        }
        val singleton = if (m.getAnnotation(Singleton::class.java) != null) m.getAnnotation(
            Singleton::class.java
        ) else m.returnType.getAnnotation(Singleton::class.java)
        val paramProviders = paramProviders(
            key,
            m.parameterTypes,
            m.genericParameterTypes,
            m.parameterAnnotations, setOf(key)
        )
        providers[key] = singletonProvider<Any>(key, singleton, Provider {
            try {
                return@Provider m.invoke(module, *params(paramProviders))
            } catch (e: Exception) {
                throw DIException(String.format("Can't instantiate %s with provider", key), e)
            }
        }
        )
    }

    private fun <T> singletonProvider(
        key: Key<*>,
        singleton: Singleton?,
        provider: Provider<T>
    ): Provider<T> {
        return if (singleton != null) Provider {
            if (!singletons.containsKey(key)) {
                synchronized(singletons) {
                    if (!singletons.containsKey(key)) {
                        singletons[key] = provider.get()!!
                    }
                }
            }
            singletons[key] as T?
        } else provider
    }

    private fun paramProviders(
        key: Key<*>,
        parameterClasses: Array<Class<*>>,
        parameterTypes: Array<Type>,
        annotations: Array<Array<Annotation>>,
        chain: Set<Key<*>>?
    ): Array<Provider<*>> {
        val providers: Array<Provider<*>?> = arrayOfNulls(parameterTypes.size)
        for (i in parameterTypes.indices) {
            val parameterClass = parameterClasses[i]
            val qualifier = qualifier(annotations[i])
            val providerType =
                if (Provider::class.java == parameterClass) (parameterTypes[i] as ParameterizedType).actualTypeArguments[0] as Class<*> else null
            if (providerType == null) {
                val newKey = Key.of(parameterClass, qualifier)
                val newChain = append(chain, key)
                if (newChain.contains(newKey)) {
                    throw DIException(
                        String.format(
                            "Circular dependency: %s",
                            chain(newChain, newKey)
                        )
                    )
                }
                providers[i] = Provider { provider(newKey, newChain).get() }
            } else {
                val newKey = Key.of(providerType, qualifier)
                providers[i] = Provider<Any?> { provider(newKey, null) }
            }
        }
        return providers as Array<Provider<*>>
    }

    companion object {
        /**
         * Constructs DI with configuration modules
         */
        fun with(vararg modules: Any?): DI {
            return DI(listOf(*modules))
        }

        /**
         * Constructs DI with configuration modules
         */
        fun with(modules: Iterable<*>): DI {
            return DI(modules)
        }

        private fun params(paramProviders: Array<Provider<*>>): Array<Any?> {
            val params = arrayOfNulls<Any>(paramProviders.size)
            for (i in paramProviders.indices) {
                params[i] = paramProviders[i].get()
            }
            return params
        }

        private fun append(set: Set<Key<*>>?, newKey: Key<*>): Set<Key<*>> {
            return if (set != null && set.isNotEmpty()) {
                val appended: MutableSet<Key<*>> =
                    LinkedHashSet(set)
                appended.add(newKey)
                appended
            } else {
                setOf(newKey)
            }
        }

        private fun injectFields(target: Class<*>): Array<Array<Any>> {
            val fields = fields(target)
            val fs: Array<Array<Any>?> = arrayOfNulls(fields.size)
            for ((i, f) in fields.withIndex()) {
                val providerType =
                    if (f.type == Provider::class.java) (f.genericType as ParameterizedType).actualTypeArguments[0] as Class<*> else null
                fs[i] = arrayOf(
                    f,
                    providerType != null,
                    Key.of(providerType ?: f.type, qualifier(f.annotations))
                )
            }
            return fs as Array<Array<Any>>
        }

        private fun fields(type: Class<*>): Set<Field> {
            var current = type
            val fields: MutableSet<Field> = HashSet()
            while (current != Any::class.java) {
                for (field in current.declaredFields) {
                    if (field.isAnnotationPresent(Inject::class.java)) {
                        field.isAccessible = true
                        fields.add(field)
                    }
                }
                current = current.superclass
            }
            return fields
        }

        private fun chain(chain: Set<Key<*>>, lastKey: Key<*>): String {
            val chainString = StringBuilder()
            for (key in chain) {
                chainString.append(key.toString()).append(" -> ")
            }
            return chainString.append(lastKey.toString()).toString()
        }

        private fun constructor(key: Key<*>): Constructor<*> {
            var inject: Constructor<*>? = null
            var noarg: Constructor<*>? = null
            for (c in key.type.declaredConstructors) {
                if (c.isAnnotationPresent(Inject::class.java)) {
                    inject = if (inject == null) {
                        c
                    } else {
                        throw DIException(
                            String.format(
                                "%s has multiple @Inject constructors",
                                key.type
                            )
                        )
                    }
                } else if (c.parameterTypes.isEmpty()) {
                    noarg = c
                }
            }
            val constructor = inject ?: noarg
            return if (constructor != null) {
                constructor.isAccessible = true
                constructor
            } else {
                throw DIException(
                    String.format(
                        "%s doesn't have an @Inject or no-arg constructor, or a module provider",
                        key.type.name
                    )
                )
            }
        }

        private fun providers(type: Class<*>): Set<Method> {
            var current = type
            val providers: MutableSet<Method> = HashSet()
            while (current != Any::class.java) {
                for (method in current.declaredMethods) {
                    if (method.isAnnotationPresent(Provides::class.java) && (type == current || !providerInSubClass(
                            method,
                            providers
                        ))
                    ) {
                        method.isAccessible = true
                        providers.add(method)
                    }
                }
                current = current.superclass
            }
            return providers
        }

        private fun qualifier(annotations: Array<Annotation>): Annotation? {
            for (annotation in annotations) {
                if (annotation is Qualifier) {
                    return annotation
                }
            }
            return null
        }

        private fun providerInSubClass(method: Method, discoveredMethods: Set<Method>): Boolean {
            for (discovered in discoveredMethods) {
                if (discovered.name == method.name && Arrays.equals(
                        method.parameterTypes,
                        discovered.parameterTypes
                    )
                ) {
                    return true
                }
            }
            return false
        }
    }

    init {
        providers[Key.of(DI::class.java)] = object : Provider<Any?> {
            override fun get(): Any {
                return this
            }
        }
        for (module in modules) {
            if (module is Class<*>) {
                throw DIException(
                    String.format(
                        "%s provided as class instead of an instance.",
                        module.name
                    )
                )
            }
            for (pM in providers(module!!.javaClass)) {
                providerMethod(module, pM)
            }
        }
    }
}

internal class DIException : RuntimeException {
    internal constructor(message: String?) : super(message)
    internal constructor(message: String?, cause: Throwable?) : super(message, cause)
}

internal class Key<T> private constructor(
    val type: Class<T>,
    val qualifier: Class<out Annotation>?,
    val name: String?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val key = other as Key<*>
        if (type != key.type) return false
        return if (qualifier != key.qualifier) false else name == key.name
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + (qualifier?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        val suffix =
            if (name != null) "@\"$name\"" else if (qualifier != null) "@" + qualifier.simpleName else ""
        return type.name + suffix
    }

    companion object {
        /**
         * @return Key for a given type
         */
        fun <T> of(type: Class<T>): Key<T> {
            return Key(type, null, null)
        }

        /**
         * @return Key for a given type and qualifier annotation type
         */
        fun <T> of(type: Class<T>, qualifier: Class<out Annotation>?): Key<T> {
            return Key(type, qualifier, null)
        }

        /**
         * @return Key for a given type and name (@Named com.tpay.sdk.api.models.blikAlias.getValue)
         */
        private fun <T> of(type: Class<T>, name: String?): Key<T> {
            return Key(type, Named::class.java, name)
        }

        fun <T> of(type: Class<T>, qualifier: Annotation?): Key<T> {
            return if (qualifier == null) {
                of(type)
            } else {
                if (qualifier is Named) of(
                    type,
                    qualifier.value
                ) else of(type, qualifier)
            }
        }
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
internal annotation class Provides

private val di = DI.with(DIModule())
internal fun Any.injectFields() {
    di.injectFields(this)
}