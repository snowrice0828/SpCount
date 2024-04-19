package k.yuki.spcount

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ViewHolderList (item: View) : RecyclerView.ViewHolder(item) {
    val spList: TextView = item.findViewById(R.id.DataId)
}