package com.tpay.sdk.internal.paymentMethod

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.tpay.sdk.R
import com.tpay.sdk.api.models.TokenizedCard
import com.tpay.sdk.api.paycard.CreditCardBrand
import com.tpay.sdk.designSystem.buttons.ButtonPayCard
import com.tpay.sdk.extensions.px
import com.tpay.sdk.extensions.updateMargins


internal class CreditCardAdapter : RecyclerView.Adapter<CreditCardAdapter.CreditCardViewHolder>() {
    var creditCardItemListener: CreditCardItemListener? = null
    private var currentlySelected: CreditCardViewHolder? = null
    var items: List<TokenizedCard> = emptyList()
        set(value) {
            notifyDataSetChanged()
            field = value
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreditCardViewHolder {
        return CreditCardViewHolder(ButtonPayCard(parent.context))
    }

    override fun onBindViewHolder(holder: CreditCardViewHolder, position: Int) {
        holder.run {
            bind(items[position])
            onClick {
                currentlySelected?.isSelected = false
                creditCardItemListener?.onClick(items[position])
                isSelected = true
                currentlySelected = holder
            }
        }
    }

    override fun getItemCount(): Int = items.size

    class CreditCardViewHolder(private val payCardButton: ButtonPayCard): RecyclerView.ViewHolder(payCardButton) {
        fun bind(tokenizedCard: TokenizedCard){
            payCardButton.run {
                payCardBrand = when(tokenizedCard.brand){
                    CreditCardBrand.MASTERCARD -> ButtonPayCard.PayCardBrand.MASTERCARD
                    CreditCardBrand.VISA -> ButtonPayCard.PayCardBrand.VISA
                    else -> ButtonPayCard.PayCardBrand.NONE
                }
                dottedCardNumber = "${context.getString(R.string.pay_card_tail_dots)} ${tokenizedCard.cardTail}"

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                updateMargins(top = HOLDER_MARGIN_TOP)
            }
        }

        var isSelected: Boolean
            get() = payCardButton.isSelected
            set(value) {
                payCardButton.isSelected = value
            }

        fun onClick(func: () -> Unit){
            payCardButton.setOnClickListener { func() }
        }

        companion object {
            private val HOLDER_MARGIN_TOP = 6.px
        }
    }

    fun interface CreditCardItemListener {
        fun onClick(tokenizedCard: TokenizedCard)
    }
}