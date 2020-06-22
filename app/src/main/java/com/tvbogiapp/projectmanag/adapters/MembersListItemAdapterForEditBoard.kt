package com.tvbogiapp.projectmanag.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tvbogiapp.projectmanag.R
import com.tvbogiapp.projectmanag.activities.EditBoardActivity
import com.tvbogiapp.projectmanag.models.User
import com.tvbogiapp.projectmanag.utils.Constants
import kotlinx.android.synthetic.main.item_member_edit_board.view.*


open class MembersListItemAdapterForEditBoard(
    private val context: Context,
    private val list: ArrayList<User>
):RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context)
                .inflate(
                    R.layout.item_member_edit_board,
                    parent,
                    false)
            )
        }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
            if(holder is MyViewHolder){

                Glide
                    .with(context)
                    .load(model.image)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(holder.itemView.iv_member_image)


                holder.itemView.tv_member_name.text = model.name
                holder.itemView.tv_member_email.text = model.email


                holder.itemView.iv_selected_member.setOnClickListener {
                    alertDialogForDeleteMember(model , model.name)
                    //TODO make dialog for deleting member from the board

                }

            }
        }
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener{
        fun onClick(position: Int, user: User, action: String)
    }

    private fun alertDialogForDeleteMember(user: User, name: String) {
        val builder = AlertDialog.Builder(context)

        builder.setTitle("Alert")

        builder.setMessage("Are you sure you want to take down $name from the member's list?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            dialogInterface.dismiss() // Dialog will be dismissed

            if (context is EditBoardActivity) {

                context.removeMemberFromList(user)
            }
        }


        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()

        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    class MyViewHolder(view:View):RecyclerView.ViewHolder(view)
}