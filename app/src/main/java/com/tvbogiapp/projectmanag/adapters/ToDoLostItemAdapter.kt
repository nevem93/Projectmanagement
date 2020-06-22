package com.tvbogiapp.projectmanag.adapters

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.text.Spannable
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.tvbogiapp.projectmanag.R
import com.tvbogiapp.projectmanag.activities.CardDetailsActivity
import com.tvbogiapp.projectmanag.models.ToDoList
import kotlinx.android.synthetic.main.dialog_add_to_do_list_item.tv_add
import kotlinx.android.synthetic.main.dialog_add_to_do_list_item.tv_cancel
import kotlinx.android.synthetic.main.dialog_change_to_do_item_name.*
import kotlinx.android.synthetic.main.item_to_do_list.view.*

open class ToDoLostItemAdapter
    (private val context: Context,
     private var list: ArrayList<ToDoList>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context)
                .inflate(
                    R.layout.item_to_do_list,
                    parent,
                    false
                )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            holder.itemView.tv_to_do_list_name.text = model.name
            holder.itemView.cb_isChecked.isChecked = model.checked

            if(model.checked){
                multiLineStrikeThrough(holder.itemView.tv_to_do_list_name, model.name)
            }

            holder.itemView.cb_isChecked.setOnClickListener {
                model.checked = !model.checked
                if(model.checked){
                    multiLineStrikeThrough(holder.itemView.tv_to_do_list_name, model.name)
                }else{
                    noStrikeThrough(holder.itemView.tv_to_do_list_name, model.name)
                }
                if (context is CardDetailsActivity){
                    context.updateAfterCheckedToDoItem(position, model.name, model.checked)
                }
            }
            holder.itemView.iv_delete_to_do_list_member.setOnClickListener {
                if(context is CardDetailsActivity){
                    alertDialogForDeleteList(position, model.name)
                }
            }

            holder.itemView.ib_edit_to_do_list_member.setOnClickListener {
                editToDoItemDialog(position, model.name)
            }


        }
    }

    private fun editToDoItemDialog(position: Int, name: String){
        if (context is CardDetailsActivity){
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.dialog_change_to_do_item_name)
            dialog.et_change_to_do_list_item.setText(name)
            dialog.tv_add.setOnClickListener {

                val toDoItem = dialog.et_change_to_do_list_item.text.toString()

                when {
                    toDoItem != name -> {
                        dialog.dismiss()
                        context.changeToDoItemName(toDoItem, position)
                    }
                    toDoItem.isEmpty() -> {
                        Toast.makeText(context,
                            "Please Enter a To Do Item Name.",
                            Toast.LENGTH_SHORT).show()
                    }
                    toDoItem == name ->
                        dialog.dismiss()
                }
            }
            dialog.tv_cancel.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }

    }
    private fun multiLineStrikeThrough(
        description: TextView,
        textContent: String
    ) {
        description.setText(textContent, TextView.BufferType.SPANNABLE)
        val spannable = description.text as Spannable
        spannable.setSpan(StrikethroughSpan(), 0, textContent.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }

    private fun noStrikeThrough(
        description: TextView,
        textContent: String
    ) {
        description.setText(textContent, TextView.BufferType.SPANNABLE)
    }

    private fun alertDialogForDeleteList(position: Int, name: String) {
        val dialog = AlertDialog.Builder(context)

        dialog.setTitle("Alert")

        dialog.setMessage("Are you sure you want to delete $name.")
        dialog.setIcon(android.R.drawable.ic_dialog_alert)

        dialog.setPositiveButton("Yes") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed

            if (context is CardDetailsActivity) {
                context.deleteToDoItemFromToDoList(position)
            }
        }


        dialog.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = dialog.create()

        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
