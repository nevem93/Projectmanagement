package com.tvbogiapp.projectmanag.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tvbogiapp.projectmanag.R
import com.tvbogiapp.projectmanag.activities.ArchiveBoardsListActivity
import com.tvbogiapp.projectmanag.models.Board
import kotlinx.android.synthetic.main.item_archive_board.view.*

open class ArchiveBoardItemsAdapterprivate (
    val context: Context,
    private var list: ArrayList<Board>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MyViewHolder(
            LayoutInflater.from(context)
                .inflate(
                    R.layout.item_archive_board,
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
            holder.itemView.tv_created_by.text = "Created By : ${model.createdBy}"


            holder.itemView.setOnClickListener {

                if (onClickListener != null) {
                    onClickListener!!.onClick(position, model)
                }
            }

            holder.itemView.btn_board_menu.setOnClickListener {
                if (context is ArchiveBoardsListActivity) {
                    val popup = PopupMenu(context, holder.itemView.btn_board_menu)
                    popup.inflate(R.menu.menu_archive_board)
                    popup.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.action_activate_board -> {
                                context.activateBoard(model)
                                true
                            }
                            R.id.action_delete_board -> {
                                context.actionDeleteBoard(model)
                                true
                            }
                            else -> false
                        }
                    }
                    popup.show()
                }
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