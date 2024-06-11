package com.tpay.sdk.internal.paymentMethod

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tpay.sdk.api.screenless.channelMethods.ChannelMethod
import com.tpay.sdk.designSystem.cards.ChannelMethodCard
import com.tpay.sdk.di.injectFields
import com.tpay.sdk.internal.Repository
import javax.inject.Inject

internal class ChannelMethodAdapter(
    private val type: ChannelMethodCard.Type
) : RecyclerView.Adapter<ChannelMethodAdapter.ChannelMethodViewHolder>() {
    @Inject
    lateinit var repository: Repository

    init {
        injectFields()
    }

    var onMethodWithImageItemClickListener: OnMethodWithImageItemClickListener? = null
    private var selectedMethodWithImageHolder: ChannelMethodViewHolder? = null
    var items = listOf<ChannelMethod>()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            notifyDataSetChanged()
            field = value
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelMethodViewHolder {
        return ChannelMethodViewHolder(ChannelMethodCard(parent.context), type)
    }

    override fun onBindViewHolder(holder: ChannelMethodViewHolder, position: Int) {
        holder.run {
            bind(items[position], repository)
            onClick {
                selectedMethodWithImageHolder?.isSelected = false
                onMethodWithImageItemClickListener?.onMethodWithImageClick(items[position].channelId)
                holder.isSelected = true
                selectedMethodWithImageHolder = holder
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ChannelMethodViewHolder(
        private val channelMethodCard: ChannelMethodCard,
        private val type: ChannelMethodCard.Type
    ) : RecyclerView.ViewHolder(channelMethodCard) {
        fun bind(method: ChannelMethod, repository: Repository) {
            channelMethodCard.type = type
            channelMethodCard.name = method.name
            repository.preloadedImages[method.imageUrl]?.run {
                channelMethodCard.logo = this
            } ?: repository.getImageDrawable(method.imageUrl).observe(
                onSuccess = { image -> channelMethodCard.logo = image },
                onError = { channelMethodCard.isNameVisible = true }
            )

            isSelected = false
        }

        var isSelected: Boolean
            get() = channelMethodCard.isSelected
            set(value) {
                channelMethodCard.isSelected = value
            }

        fun onClick(func: () -> Unit) {
            channelMethodCard.setOnClickListener { func() }
        }
    }

    fun interface OnMethodWithImageItemClickListener {
        fun onMethodWithImageClick(id: Int)
    }
}