package com.tpay.sdk.internal.paymentMethod

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.tpay.sdk.api.screenless.blik.AmbiguousAlias
import com.tpay.sdk.designSystem.buttons.ButtonPaymentMethod
import com.tpay.sdk.extensions.px
import com.tpay.sdk.extensions.updateMargins


internal class AmbiguousBLIKAdapter : RecyclerView.Adapter<AmbiguousBLIKAdapter.AmbiguousBLIKViewHolder>() {
    var ambiguousBlikOnClickListener: AmbiguousBLIKOnClickListener? = null
    private var selectedAmbiguousBLIKHolder: AmbiguousBLIKViewHolder? = null
    var items: List<AmbiguousAlias> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            notifyDataSetChanged()
            field = value
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AmbiguousBLIKViewHolder {
        return AmbiguousBLIKViewHolder(ButtonPaymentMethod(parent.context))
    }

    override fun onBindViewHolder(holder: AmbiguousBLIKViewHolder, position: Int) {
        holder.run {
            val data = items[position]
            bind(data.name)
            onClick {
                selectedAmbiguousBLIKHolder?.isSelected = false
                ambiguousBlikOnClickListener?.onClick(data)
                holder.isSelected = true
                selectedAmbiguousBLIKHolder = holder
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    internal class AmbiguousBLIKViewHolder(private val view: ButtonPaymentMethod) : RecyclerView.ViewHolder(view) {
        fun bind(bankName: String){
            view.run {
                name = bankName
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                updateMargins(top = HOLDER_MARGIN_TOP)
            }
            isSelected = false
        }

        var isSelected: Boolean
            get() = view.isSelected
            set(value) {
                view.isSelected = value
            }

        fun onClick(func: () -> Unit){
            view.setOnClickListener { func() }
        }

        companion object {
            private val HOLDER_MARGIN_TOP = 6.px
        }
    }

    fun interface AmbiguousBLIKOnClickListener {
        fun onClick(ambiguousAlias: AmbiguousAlias)
    }
}