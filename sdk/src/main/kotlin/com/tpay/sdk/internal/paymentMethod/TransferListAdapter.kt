package com.tpay.sdk.internal.paymentMethod

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tpay.sdk.designSystem.cards.CardBank


internal class TransferListAdapter : RecyclerView.Adapter<TransferListAdapter.TransferViewHolder>() {
    var onTransferItemClickListener: OnTransferItemClickListener? = null
    private var selectedBankHolder: TransferViewHolder? = null
    var items = listOf<TransferMethod>()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            notifyDataSetChanged()
            field = value
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransferViewHolder {
        return TransferViewHolder(CardBank(parent.context))
    }

    override fun onBindViewHolder(holder: TransferViewHolder, position: Int) {
        holder.run {
            bind(items[position])
            onClick {
                selectedBankHolder?.isSelected = false
                onTransferItemClickListener?.onTransferItemClick(items[position].id)
                holder.isSelected = true
                selectedBankHolder = holder
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class TransferViewHolder(private val cardBank: CardBank) : RecyclerView.ViewHolder(cardBank) {
        fun bind(transferMethod: TransferMethod) {
            cardBank.bankName = transferMethod.name
            cardBank.bankLogo = transferMethod.icon
            isSelected = false
        }

        var isSelected: Boolean
            get() = cardBank.isSelected
            set(value) {
                cardBank.isSelected = value
            }

        fun onClick(func: () -> Unit) {
            cardBank.setOnClickListener { func() }
        }
    }

    fun interface OnTransferItemClickListener {
        fun onTransferItemClick(id: String)
    }
}

internal data class TransferMethod(
    val id: String,
    val name: String,
    val icon: Drawable? = null
)