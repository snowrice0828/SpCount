package com.snowrice0828.spcount

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.view.View


class RecyclerAdapter(val list: Array<MainActivity.Record>) : RecyclerView.Adapter<ViewHolderList>() {
    // リスナー格納変数
    lateinit var listener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): ViewHolderList {
        var itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return ViewHolderList(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolderList, position: Int) {
        holder.ItemId.text = (list[position]).id.toString()
        holder.ItemName.text = (list[position]).name.toString()
        holder.ItemContents.text = (list[position]).contents.toString()
        holder.ItemRemarks.text = (list[position]).remarks.toString().replace("\n", " ")

        // タップしたとき
        holder.itemView.setOnClickListener {
            listener.onItemClickListener(it, position, (list[position]).id)
        }
    }

    override fun getItemCount(): Int = list.size

    //インターフェースの作成
    interface OnItemClickListener{
        fun onItemClickListener(view: View, position: Int, clickedId: Int)
    }

    // リスナー
    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }


}