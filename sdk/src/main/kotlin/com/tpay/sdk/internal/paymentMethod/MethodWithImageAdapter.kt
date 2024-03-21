package com.tpay.sdk.internal.paymentMethod

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tpay.sdk.designSystem.cards.MethodWithImageCard
import com.tpay.sdk.internal.model.MethodWithImage


internal class MethodWithImageAdapter(
    private val type: MethodWithImageCard.Type
) : RecyclerView.Adapter<MethodWithImageAdapter.MethodWithImageViewHolder>() {
    var onMethodWithImageItemClickListener: OnMethodWithImageItemClickListener? = null
    private var selectedMethodWithImageHolder: MethodWithImageViewHolder? = null
    var items = listOf<MethodWithImage>()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            notifyDataSetChanged()
            field = value
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MethodWithImageViewHolder {
        return MethodWithImageViewHolder(MethodWithImageCard(parent.context), type)
    }

    override fun onBindViewHolder(holder: MethodWithImageViewHolder, position: Int) {
        holder.run {
            bind(items[position])
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

    class MethodWithImageViewHolder(
        private val methodWithImageCard: MethodWithImageCard,
        private val type: MethodWithImageCard.Type
    ) : RecyclerView.ViewHolder(methodWithImageCard) {
        fun bind(method: MethodWithImage) {
            methodWithImageCard.type = type
            methodWithImageCard.name = method.name
            methodWithImageCard.logo = method.image
            isSelected = false
        }

        var isSelected: Boolean
            get() = methodWithImageCard.isSelected
            set(value) {
                methodWithImageCard.isSelected = value
            }

        fun onClick(func: () -> Unit) {
            methodWithImageCard.setOnClickListener { func() }
        }
    }

    fun interface OnMethodWithImageItemClickListener {
        fun onMethodWithImageClick(id: Int)
    }
}