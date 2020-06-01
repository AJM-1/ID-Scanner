package com.example.idscanner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.codelab.barcode_scanning.R
import kotlinx.android.synthetic.main.item_row.view.*
import org.joda.time.LocalDate
import org.joda.time.Period
import org.joda.time.PeriodType
import java.lang.Exception

class QrCodeAdapter(private val qrList: ArrayList<QrCode>) : RecyclerView.Adapter<QrCodeAdapter.QrHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QrHolder {
        return QrHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false))
    }

    override fun getItemCount() = qrList.size

    override fun onBindViewHolder(holder: QrHolder, position: Int) {
        with(qrList[position]) {
            holder.itemView.qrValue.text = this.value
            holder.itemView.qrName.text = this.name
            holder.itemView.qrBirthDate.text = dateFormat(this.age)
            holder.itemView.qrExpiration.text = isExpired(this.exp)
            holder.itemView.qrLine1.text = this.street
            holder.itemView.qrLine2.text = this.city + " " + this.state + " " + this.zip!!.substring(0, this.zip.length - 4)
            holder.itemView.qrTwentyOne.text = isTwentyOne(this.age)

        }
    }

    private fun isTwentyOne(birth: String?): String {
        try {
            val date = birth.toString()
            val birthDate = LocalDate(date.substring(4, 8).toInt(), date.substring(0, 2).toInt(), date.substring(2, 4).toInt())

            return "Age: " + Period(birthDate, LocalDate(), PeriodType.yearMonthDay()).years
        } catch (e: Exception) {
            return "INVALID DATE"
        }
    }

    private fun isExpired(exp: String?): String {
        try {
            val date = exp.toString()
            val expiration = LocalDate(date.substring(4, 8).toInt(), date.substring(0, 2).toInt(), date.substring(2, 4).toInt())

            if (LocalDate().isAfter(expiration)) {
                return "Expired"
            }

            return "Not Expired"
        } catch (e: Exception) {
            return "INVALID DATE"
        }
    }

    private fun dateFormat(birth: String?): String {
        val date = birth.toString()
        return date.substring(0, 2) + "/" + date.substring(2, 4) + "/" + date.substring(4, 8)
    }


    class QrHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}