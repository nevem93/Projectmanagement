package com.tvbogiapp.projectmanag.adapters

import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import android.view.*
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tvbogiapp.projectmanag.R
import com.tvbogiapp.projectmanag.activities.*
import com.tvbogiapp.projectmanag.models.Board
import com.tvbogiapp.projectmanag.utils.Constants

import kotlinx.android.synthetic.main.item_board.view.*

open class BoardItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Board>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MyViewHolder(
            LayoutInflater.from(context)
                .inflate(
                R.layout.item_board,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {

            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(holder.itemView.iv_board_image)

            holder.itemView.tv_name.text = model.name
            holder.itemView.tv_created_by.text = "Created By : ${model.createdBy}"}

        holder.itemView.setOnClickListener {

            if (onClickListener != null) {
                onClickListener!!.onClick(position, model)
            }
        }

        holder.itemView.btn_board_menu.setOnClickListener {
            if (context is MainActivity) {
                val popup = PopupMenu(context, holder.itemView.btn_board_menu)
                popup.inflate(R.menu.menu_board)
                popup.setOnMenuItemClickListener(object :
                    PopupMenu.OnMenuItemClickListener {
                    override fun onMenuItemClick(item: MenuItem): Boolean {
                        when (item.itemId) {
                            R.id.action_members -> {
                                context.actionMembers(model)
                                return true
                            }
                            R.id.action_edit_table -> {
                                context.actionEditTable(model, model.assignedTo)
                                return true
                            }
                            R.id.action_delete_table -> {
                                context.actionDeleteBoard(model)
                                return true
                            }
                            R.id.action_archive_table -> {
                                context.actionArchiveTable(model)
                                return true
                            }
                            else -> return false
                        }
                    }
                }
                )
                popup.show()
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, model: Board)
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}




