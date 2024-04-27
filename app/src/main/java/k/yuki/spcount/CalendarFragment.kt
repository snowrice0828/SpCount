package k.yuki.spcount
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

class CalendarFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var helper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        helper = DatabaseHelper(view.context)
        super.onViewCreated(view, savedInstanceState)
        // ここでカレンダーの初期化や設定を行う
        var getList = helper.selectDataAll()
        recyclerView = view.findViewById<RecyclerView>(R.id.dataListView)
        recyclerView.adapter = RecyclerAdapter(getList)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        //https://qiita.com/soutominamimura/items/47a48e4e6e1aff3d3396
    }


}