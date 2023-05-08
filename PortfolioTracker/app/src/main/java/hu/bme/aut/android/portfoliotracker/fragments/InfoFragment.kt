package hu.bme.aut.android.portfoliotracker.fragments

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.DashPathEffect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import hu.bme.aut.android.portfoliotracker.R
import hu.bme.aut.android.portfoliotracker.User
import hu.bme.aut.android.portfoliotracker.databinding.FragmentInfoBinding
import hu.bme.aut.android.portfoliotracker.stockInfo.model.*
import hu.bme.aut.android.portfoliotracker.stockInfo.network.CallbackClass
import hu.bme.aut.android.portfoliotracker.stockInfo.network.NetworkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*


class InfoFragment : Fragment() {
    companion object{
        private const val DAYS_IN_A_YEAR = 365
        private const val DAYS_IN_A_MONTH = 31
        private const val DAYS_IN_5_YEARS = DAYS_IN_A_YEAR * 5
    }

    private lateinit var binding : FragmentInfoBinding
    private var symbol: String? = null
    private lateinit var callbackClassOverview : CallbackClass<StockOverview>
    private lateinit var callbackClassChartDay : CallbackClass<TimeSeriesIntraday>
    private lateinit var callbackClassChartLongTerm : CallbackClass<TimeSeries>

    var firstLongTermLoad = true

    private var entryListLongTerm = ArrayList<Entry>()

    private lateinit var chart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInfoBinding.inflate(inflater, container, false)
        callbackClassOverview = CallbackClass(binding, ::assignOverviewData)
        callbackClassChartDay = CallbackClass(binding, ::loadDataAndDrawDayChart)
        callbackClassChartLongTerm = CallbackClass<TimeSeries>(binding, ::loadCharDataForLongTerm)

        binding.swiperefresh.isVisible = false
        binding.progressBar.isVisible= true
        chart = binding.chart1
        symbol = arguments?.getString("symbol")


        binding.buttonToggle.setOnClickListener {
            if (binding.expandableTextView.isExpanded)
            {
                binding.buttonToggle.text = resources.getString(R.string.expand)
            }
            else{
                binding.buttonToggle.text = resources.getString(R.string.collapse)
            }
            binding.expandableTextView.toggle()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.swiperefresh.setOnRefreshListener {
            val stockOverview = NetworkManager.getStockOverview(symbol)
            stockOverview.enqueue(callbackClassOverview)
            binding.swiperefresh.isRefreshing = false
        }

        binding.buyButton.setOnClickListener {
            buildAndShowBuyDialog()
        }
        binding.sellButton.setOnClickListener {
            buildAndShowSellDialog()
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {

                lifecycleScope.launch(Dispatchers.Main) {
                    withContext(Dispatchers.IO) {
                        if (firstLongTermLoad) {
                            val data = NetworkManager.getTimeSeries(symbol, "full")

                            val response = data.execute()
                            loadCharDataForLongTerm(response.body())
                            firstLongTermLoad = false
                        }
                    }


                    when (tab?.position) {
                        GraphView.DAILY.ordinal -> {
                            val dayData = NetworkManager.getTimeSeriesIntraday(symbol, "compact")
                            dayData.enqueue(callbackClassChartDay)
                        }
                        GraphView.MONTHLY.ordinal -> {

                            setUpChartLong(GraphView.MONTHLY)
                        }
                        GraphView.YEARLY.ordinal -> {
                            setUpChartLong(GraphView.YEARLY)

                        }
                        GraphView.ALL.ordinal -> {
                            setUpChartLong(GraphView.ALL)
                        }
                    }

                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })

        val stockOverview = NetworkManager.getStockOverview(symbol)
        stockOverview.enqueue(callbackClassOverview)
        val dayData = NetworkManager.getTimeSeriesIntraday(symbol,"compact")
        dayData.enqueue(callbackClassChartDay)

        super.onViewCreated(view, savedInstanceState)

    }

    private fun buildAndShowBuyDialog() {
        val alertDialog: AlertDialog.Builder =
            AlertDialog.Builder(this@InfoFragment.context)
        alertDialog.setTitle("How many shares do you wish to buy?")
        alertDialog.setMessage("Please select the amount of shares you want to acquire!\nPrice per share: ${binding.strPrice.text}\nYour balance: ${User.getBalance()}")

        val view = layoutInflater.inflate(R.layout.dialog_buy_activity,null,false)
        alertDialog.setView(view)
        val editText = view.findViewById<EditText>(R.id.buyTextEdit)
        val sellTextView = view.findViewById<TextView>(R.id.strBuyAmount)

        editText.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val price = binding.strPrice.text.toString().toDouble()
                val quantity = if(editText.text.toString() == "") 0.0 else editText.text.toString().toDouble()
                val amount = price * quantity
                sellTextView.text = amount.toString()
            }
        })

        alertDialog.setNeutralButton("Max"){_,_-> }
        alertDialog.setPositiveButton("Ok") { _, _ ->
            run {
                val num = editText.text.toString().toDouble()
                val price = binding.strPrice.text.toString().toDouble()
                val cost = num * price
                if( cost > User.getBalance() ) {
                    Snackbar.make(binding.root,"You cannot afford that. Transaction Cancelled!",Snackbar.LENGTH_LONG).show()
                    return@run
                }
                var stockOwned = User.stocksOwned.find { x ->x.symbol == symbol }
                if(stockOwned == null)
                {
                    stockOwned = StockOwned(symbol!!,num)
                    User.stocksOwned.add(stockOwned)
                }
                else{
                    stockOwned.amount +=num
                }

                val stockBought= StockHistory(symbol!!, num,cost, LocalDate.now(), true)
                stockOwned.stockHistory.add(stockBought)

                User.deductFromBalance(cost)

                var listHistory = User.transactionHistory[stockBought.date]
                if(listHistory == null) {
                    listHistory = ArrayList()
                    User.transactionHistory[stockBought.date] = listHistory
                }
                listHistory.add(stockBought)

                lifecycleScope.launch(Dispatchers.Main){
                    withContext(Dispatchers.IO){
                        User.database.stockHistoryDAO().insert(stockBought)

                        val stockFound = User.database.stockOwnedDAO().getStockWithID(stockOwned.id)
                        if( stockFound == null)
                            User.database.stockOwnedDAO().insert(stockOwned)
                        else{
                            User.database.stockOwnedDAO().update(stockOwned)
                        }
                    }
                }
                saveBalance()
            }
        }

        alertDialog.setNegativeButton(
            "Cancel"
        ) { _, _ -> }

        val dialog = alertDialog.create()
        dialog.show()

        val button = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
        button?.setOnClickListener {
            val maxValue = User.getBalance() / binding.strPrice.text.toString().toDouble()
            editText.setText(String.format("%.2f",maxValue,DecimalFormatSymbols(Locale.ENGLISH)))
        }
    }
    private fun buildAndShowSellDialog(){
        val alertDialog: AlertDialog.Builder =
            AlertDialog.Builder(this@InfoFragment.context)
        alertDialog.setTitle("How many shares do you wish to sell?")
        alertDialog.setMessage("Please select the amount of shares you want to sell!\nPrice per share: ${binding.strPrice.text}")


        val view = layoutInflater.inflate(R.layout.dialog_sell_activity,null,false)
        alertDialog.setView(view)
        val sellLabel = view.findViewById<TextView>(R.id.strSellAmount)
        val editText = view.findViewById<EditText>(R.id.sellTextEdit)

        editText.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val price = binding.strPrice.text.toString().toDouble()
                val quantity = if(editText.text.toString() == "") 0.0 else editText.text.toString().toDouble()
                val amount = price * quantity
                sellLabel.text = amount.toString()
            }
        })

        alertDialog.setNeutralButton("All"){_,_-> }
        alertDialog.setPositiveButton("Ok") { _, _ ->
            run {
                val num = editText.text.toString().toDouble()
                val stockOwned = User.stocksOwned.find { x->x.symbol == symbol }
                if( stockOwned == null || num > stockOwned.amount)  {
                    Snackbar.make(binding.root,"That is more than you own. Transaction Cancelled!", Snackbar.LENGTH_LONG).show()
                    return@run
                }
                val price = num*binding.strPrice.text.toString().toDouble()
                val stockSold = StockHistory(symbol!!,num,price, LocalDate.now(),false)
                stockOwned.amount-=num
                stockOwned.stockHistory.add(stockSold)
                User.addToBalance(price)

                val listHistory: ArrayList<StockHistory> = User.transactionHistory[stockSold.date]
                    ?: throw Error("Stock History cannot be empty!")

                listHistory.add(stockSold)

                lifecycleScope.launch(Dispatchers.Main){
                    withContext(Dispatchers.IO){
                        User.database.stockHistoryDAO().insert(stockSold)
                    }
                }

                saveBalance()
            }
        }

        alertDialog.setNegativeButton(
            "Cancel"
        ) { _, _ -> }

        val dialog = alertDialog.create()
        dialog.show()

        val button = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
        button?.setOnClickListener {
            val stockOwned = User.stocksOwned.find { x->x.symbol==symbol } ?: return@setOnClickListener
            val maxValue = stockOwned.amount
            editText.setText(String.format("%.2f",maxValue,DecimalFormatSymbols(Locale.ENGLISH)))
        }


    }
    private fun assignOverviewData(overview: StockOverview?){

        (overview?.Name + " ("+overview?.Symbol+")").also { binding.strTitle.text = it }
        binding.expandableTextView.text = overview?.Description

        binding.countryText.text=overview?.Country
        binding.currencyText.text=overview?.Currency
        binding.exchangeText.text=overview?.Exchange
        binding.high52weekText.text=overview?.WeekHigh52
        binding.low52weekText.text=overview?.WeekLow52
        binding.sectorText.text=overview?.Sector
    }
    private fun loadDataAndDrawDayChart(timeSeriesIntraday: TimeSeriesIntraday?){
        val price = timeSeriesIntraday?.timeStamps?.toList()?.get(0)?.second?.price?.toDouble()
        val currentDay = timeSeriesIntraday?.timeStamps?.toList()?.get(0)?.first?.split(" ")?.get(0)
        binding.strPrice.text = resources.getString(R.string.currentprice,price)
        initChart()


        val values: ArrayList<Entry> = ArrayList()
        val initialList = timeSeriesIntraday?.timeStamps?.toList()
        val list = initialList?.sortedBy { it.first?.toDate() }
        var yesterdayClose = list?.get(0)?.second?.price?.toDouble()!!
        for (i in 0 until list.count()) {
            val key = list[i].first?.split(" ")?.get(0)
            if (key != currentDay) {
                yesterdayClose = list[i].second?.price?.toDouble()!!
                continue
            }
            val value = list[i].second?.price?.toFloat()!!
            values.add(Entry(i.toFloat(), value))
        }

        lifecycleScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                setUpChart(values)
            }
        }

        var change = price?.div(yesterdayClose)
        change = change?.times(100)
        change = change?.minus(100.0)

        binding.strChange.text = resources.getString(R.string.percentChange,change)

        binding.swiperefresh.isVisible = true
        binding.progressBar.isVisible= false
    }

    private fun loadCharDataForLongTerm(timeSeries: TimeSeries?){
        val initialList = timeSeries?.timeStamps?.toList()
        val list = initialList?.sortedBy { it.first?.toDateSimple() }
        for (i in 0 until list?.count()!!) {
            if( i >= 5 * DAYS_IN_A_YEAR){
                break
            }
            val value = list[i].second?.price?.toFloat()!!
            entryListLongTerm.add(Entry(i.toFloat(), value))
        }

    }

    private fun setUpChartLong(interval : GraphView ){
        chart.clear()
        initChart()
        var count : Int = when(interval){
            GraphView.MONTHLY->{
                DAYS_IN_A_MONTH
            }
            GraphView.YEARLY->{
                DAYS_IN_A_YEAR
            }
            GraphView.ALL->{
                DAYS_IN_5_YEARS
            }
            else -> {
                0
            }
        }

        lifecycleScope.launch(Dispatchers.Main){
            withContext(Dispatchers.IO){
                if(count < entryListLongTerm.count())
                {
                    setUpChart(entryListLongTerm.subList(0,count))
                }
                else if (entryListLongTerm.isNotEmpty()){
                    setUpChart(entryListLongTerm.subList(0,entryListLongTerm.count()-1))
                }
            }
        }

    }

    private fun initChart() {
        chart.setBackgroundColor(Color.WHITE)
        chart.description.isEnabled = false
        chart.setTouchEnabled(true)
        chart.setDrawGridBackground(false)
        chart.isDragEnabled = true
        chart.isScaleXEnabled = true
        chart.isScaleYEnabled = true
        chart.setPinchZoom(true)

        val xAxis = chart.xAxis
        xAxis.enableAxisLineDashedLine(10f, 10f, 0f)

        val yAxis = chart.axisLeft
        chart.axisRight.isEnabled = false
        yAxis.enableAxisLineDashedLine(10f, 10f, 0f)
    }

    private fun setUpChart(values: MutableList<Entry>) {
        val set1: LineDataSet
        if (chart.data != null && chart.data.dataSetCount > 0) {
            set1 = chart.data.getDataSetByIndex(0) as LineDataSet
            set1.values = values
            set1.notifyDataSetChanged()
            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()
        } else {
            set1 = LineDataSet(values, "Price per share")

            set1.setDrawIcons(false)

            set1.enableDashedLine(10f, 5f, 0f)
            set1.color = Color.BLACK
            set1.setCircleColor(Color.BLACK)

            set1.lineWidth = 1f
            set1.circleRadius = 3f

            set1.formLineWidth = 1f

            set1.formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
            set1.formSize = 15f

            set1.valueTextSize = 9f

            set1.enableDashedHighlightLine(10f, 5f, 0f)

            set1.setDrawFilled(true)
            set1.fillFormatter =
                IFillFormatter { _, _ -> chart.axisLeft.axisMinimum }
        }

        set1.fillColor = Color.GREEN

        val dataSet = listOf<ILineDataSet>(set1)
        val lineData = LineData(dataSet)
        chart.data = lineData
        chart.invalidate()
    }

    private fun saveBalance(){
        val pref = context?.getSharedPreferences("app_prefs",Activity.MODE_PRIVATE)
        val editor = pref?.edit()
        editor?.putFloat("user_balance",User.getBalance().toFloat())
        editor?.apply()
    }

    private fun String.toDate(): Date? {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(this)
    }
    private fun String.toDateSimple() : Date?{
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(this)
    }

    private enum class GraphView{
        DAILY,
        MONTHLY,
        YEARLY,
        ALL
    }

}