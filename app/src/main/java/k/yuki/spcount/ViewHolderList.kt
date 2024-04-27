package k.yuki.spcount

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ViewHolderList (item: View) : RecyclerView.ViewHolder(item) {
    val ItemId: TextView = item.findViewById(R.id.item_ID)
    val ItemContents: TextView = item.findViewById(R.id.item_contents)
    val ItemName: TextView = item.findViewById(R.id.item_name)
    val ItemRemarks: TextView = item.findViewById(R.id.item_remarks)
}