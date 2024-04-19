package k.yuki.spcount

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class RecyclerAdapter(val list: Array<MainActivity.Record>) : RecyclerView.Adapter<ViewHolderList>() {
    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): ViewHolderList {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return ViewHolderList(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolderList, position: Int) {
        holder.spList.text = (list[position]).id.toString()
    }

    override fun getItemCount(): Int = list.size
}