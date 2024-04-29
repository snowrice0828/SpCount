package com.snowrice0828.spcount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar


class CalendarFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var helper: DatabaseHelper

    private var SelectDate :Int = 0
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val calendar = Calendar.getInstance()
        calendar.set(2024, 1, 7)        // テストとしてデータの入っている日付を初期値としてセット

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // 月は0から始まるため、1を加算して正しい月を取得します
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        SelectDate = year * 10000 + month * 100 + day

        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        helper = DatabaseHelper(view.context)
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById<RecyclerView>(R.id.dataListView)
        val calendarView = view.findViewById<CalendarView>(R.id.Calendar)
        val calendar = Calendar.getInstance()
        calendar.set(SelectDate / 10000, (SelectDate / 100) % 100 - 1, SelectDate % 100)
        calendarView.date = calendar.timeInMillis
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            SelectDate = year * 10000 + (month + 1) * 100 + dayOfMonth
            setRecylerView()
        }
        // 初期日付でのデータ取得表示
        setRecylerView()
    }

    fun setRecylerView()
    {
        var getList = helper.selectDataYmd(SelectDate)
        val adapter: RecyclerAdapter = RecyclerAdapter(getList)

        //https://qiita.com/soutominamimura/items/47a48e4e6e1aff3d3396
        // インターフェースの実装
        adapter.setOnItemClickListener(object:RecyclerAdapter.OnItemClickListener{
            override fun onItemClickListener(view: View, position: Int, clickedId: Int) {
                clickItem(view, clickedId)
            }
        })

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        val dividerItemDecoration = DividerItemDecoration(requireActivity() , LinearLayoutManager(requireActivity()).getOrientation())
        recyclerView.addItemDecoration(dividerItemDecoration)
    }

    fun clickItem(view: View, ItemId: Int)
    {
        Toast.makeText(requireActivity(), "ID:${ItemId}", Toast.LENGTH_LONG).show()

        // Bundle（オブジェクトの入れ物）のインスタンスを作成する
        val bundle = Bundle()
        // Key/Pairの形で値をセットする
        bundle.putInt("KEY_ID", ItemId)
        // Fragmentに値をセットする
        val fragment = HomeFragment()
        fragment.setArguments(bundle)
        // 遷移処理
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .setReorderingAllowed(true)
            .commit()
    }

}