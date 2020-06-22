package com.tvbogiapp.projectmanag.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.tvbogiapp.projectmanag.R
import com.tvbogiapp.projectmanag.adapters.CardMemberListItemsAdapter
import com.tvbogiapp.projectmanag.adapters.ToDoLostItemAdapter
import com.tvbogiapp.projectmanag.dialogs.LabelColorListDialog
import com.tvbogiapp.projectmanag.dialogs.MembersListDialog
import com.tvbogiapp.projectmanag.firebase.FireStoreClass
import com.tvbogiapp.projectmanag.models.*
import com.tvbogiapp.projectmanag.utils.Constants
import kotlinx.android.synthetic.main.activity_card_details.*
import kotlinx.android.synthetic.main.dialog_add_to_do_list_item.*
import kotlinx.android.synthetic.main.dialog_add_to_do_list_item.tv_add
import kotlinx.android.synthetic.main.dialog_add_to_do_list_item.tv_cancel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CardDetailsActivity : BaseActivity() {

    private lateinit var mBoardDetails: Board
    private var mTaskListPositon = -1
    private var mCardPosition = -1
    private var mSelectedColor = ""
    private lateinit var mMembersDetailList: ArrayList<User>
    private var mSelectedDueDateMilliSeconds: Long = 0
    private lateinit var mToDoList :ArrayList<ToDoList>

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)
        getIntentData()
        setupActionBar()
        setupSelectedMembersList()
        setupToDoList()

        tv_card_title.text = mBoardDetails
            .taskList[mTaskListPositon]
            .cards[mCardPosition]
            .name

        ib_edit_card_name.setOnClickListener {
            et_name_card_details.setText(mBoardDetails
                .taskList[mTaskListPositon]
                .cards[mCardPosition]
                .name)

            ll_card_title_view.visibility =View.GONE
            ll_card_title_edit_view.visibility = View.VISIBLE
        }
        ib_done_card_name.setOnClickListener{
            if (et_name_card_details.text.isNotEmpty()){
                mBoardDetails.taskList[mTaskListPositon]
                    .cards[mCardPosition].name = et_name_card_details.text.toString()
                tv_card_title.text = mBoardDetails.taskList[mTaskListPositon]
                    .cards[mCardPosition].name
                ll_card_title_view.visibility =View.VISIBLE
                ll_card_title_edit_view.visibility = View.GONE
                updateCardDetails()
            }
            else {
                Toast.makeText(this,
                    "Please enter a board name.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


        mSelectedColor = mBoardDetails.taskList[mTaskListPositon].cards[mCardPosition].labelColor
        if(mSelectedColor.isNotEmpty()){
            setColor()
        }

        btn_update_card_details.setOnClickListener {
            if(et_name_card_details.text.toString().isNotEmpty()){

                updateCardDetails()
            } else
            {
                Toast.makeText(this@CardDetailsActivity,
                "Enter a card name.", Toast.LENGTH_SHORT).show()
            }
        }
        tv_select_label_color.setOnClickListener {
            labelColorsListDialog()
        }
        tv_select_members.setOnClickListener {
            membersListDialog()
        }

        tv_add_to_do_item.setOnClickListener {
            toDoListDialog()
        }

        mSelectedDueDateMilliSeconds =
            mBoardDetails.taskList[mTaskListPositon]
                .cards[mCardPosition]
                .dueDate

        if(mSelectedDueDateMilliSeconds > 0){
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val selectedDate= simpleDateFormat.format(Date(mSelectedDueDateMilliSeconds))
            tv_select_due_date.text = selectedDate
        }

        tv_select_due_date.setOnClickListener {
            showDatePicker()
        }
    }

    private fun deleteCard(){
        val cardsList: ArrayList<Card> = mBoardDetails
            .taskList[mTaskListPositon].cards
        cardsList.removeAt(mCardPosition)
        val taskList: ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size-1)

        taskList[mTaskListPositon].cards = cardsList

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@CardDetailsActivity, mBoardDetails)

    }

    private fun alertDialogForDeletedCard(cardName: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.alert))
        builder.setMessage(
            resources.getString(
                R.string.confirmation_message_to_delete_card,
                cardName
            )
        )
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton(resources.getString(R.string.yes)){dialogInterface, _ ->
            dialogInterface.dismiss()
            deleteCard()
        }
        builder.setNegativeButton(resources.getString(R.string.no)){dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    fun addUpdateTaskListSuccess(){
        setResult(Activity.RESULT_OK)
        setupActionBar()
    }


    private fun setupActionBar(){
        setSupportActionBar(toolbar_card_details_activity)

        val actionBar = supportActionBar
        if( actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = mBoardDetails
                .taskList[mTaskListPositon]
                .cards[mCardPosition]
                .name

        }

        toolbar_card_details_activity.setNavigationOnClickListener {
            onBackPressed()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun colorsList():ArrayList<String>{
        val colorsList : ArrayList<String> = ArrayList()
        colorsList.add("#43C86F")
        colorsList.add("#0C90F1")
        colorsList.add("#F72400")
        colorsList.add("#7A8089")
        colorsList.add("#D57C1D")
        colorsList.add("#770000")
        colorsList.add("#0022F8")
        return colorsList
    }

    private fun setColor(){
        tv_select_label_color.text = ""
        tv_select_label_color.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId){
            R.id.action_delete_card->{
                alertDialogForDeletedCard(mBoardDetails.taskList[mTaskListPositon].cards[mCardPosition].name)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getIntentData(){
        if(intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }
        if(intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)){
            mTaskListPositon = intent.getIntExtra(
                Constants.TASK_LIST_ITEM_POSITION, -1)
        }
        if(intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)){
            mCardPosition = intent.getIntExtra(
                Constants.CARD_LIST_ITEM_POSITION,-1)
        }
        if(intent.hasExtra(Constants.BOARD_MEMBERS_LIST)){
            mMembersDetailList = intent.getParcelableArrayListExtra(
                Constants.BOARD_MEMBERS_LIST)!!
        }
        if(intent.hasExtra(Constants.TO_DO_LIST)){
            mToDoList = intent.getParcelableArrayListExtra(
                Constants.TO_DO_LIST)!!
        }

    }

    private fun toDoListDialog(){
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_to_do_list_item)
        dialog.tv_add.setOnClickListener {
            val toDoItem = dialog.et_add_to_do_list_item.text.toString()

            if(toDoItem.isNotEmpty()){
                dialog.dismiss()
                addToDoListItemToCard(toDoItem)

            }else{
                Toast.makeText(this,
                    "Please Enter a To Do Item Name.",
                    Toast.LENGTH_SHORT).show()
            }

        }
        dialog.tv_cancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }


    private fun addToDoListItemToCard(toDoItemName: String) {

        val toDoItem = ToDoList(toDoItemName, FireStoreClass().getCurrentUserID(),false)

        mBoardDetails
            .taskList[mTaskListPositon]
            .cards[mCardPosition]
            .toDoList.add(0, toDoItem)
        updateCardDetails()
    }

    fun updateAfterCheckedToDoItem(itemPosition: Int, itemName:String, checkResult: Boolean){
        updateCardDetails()
    }

    fun deleteToDoItemFromToDoList(position:Int){
        mBoardDetails.taskList[mTaskListPositon].cards[mCardPosition].toDoList.removeAt(position)
        mBoardDetails.taskList[mTaskListPositon].cards[mCardPosition].toDoList.removeAt(
            mBoardDetails.taskList[mTaskListPositon].cards[mCardPosition].toDoList.size-1)
        updateCardDetails()
    }
     fun changeToDoItemName(toDoItemNewName:String, position: Int){
         mBoardDetails.taskList[mTaskListPositon].cards[mCardPosition].toDoList[position].name = toDoItemNewName
         updateCardDetails()
     }


    private fun setupToDoList(){

        val cardAssignedToDoList =
            mBoardDetails.taskList[mTaskListPositon].cards[mCardPosition].toDoList

        rv_to_do_list.layoutManager = LinearLayoutManager(this)
        rv_to_do_list.setHasFixedSize(true)
        rv_to_do_list.visibility = View.VISIBLE


        val adapter = ToDoLostItemAdapter(this, cardAssignedToDoList)
        rv_to_do_list.adapter = adapter


    }
    private fun membersListDialog(){
        val cardAssignedMembersList = mBoardDetails.taskList[mTaskListPositon]
            .cards[mCardPosition].assignedTo

        if(cardAssignedMembersList.size>0){
            for (i in mMembersDetailList.indices){
                for (j in cardAssignedMembersList){
                    if(mMembersDetailList[i].id == j){
                        mMembersDetailList[i].selected = true
                    }
                }
            }
        }else{
            for (i in mMembersDetailList.indices){
                mMembersDetailList[i].selected = false
            }
        }

        val listDialog= object: MembersListDialog (
            this,
            mMembersDetailList,
            resources.getString(R.string.str_select_member)
        ){
            override fun onItemSelected(user: User, action: String) {
                if (action == Constants.SELECT){
                    if (!mBoardDetails.taskList[mTaskListPositon]
                            .cards[mCardPosition]
                            .assignedTo.contains(user.id)){
                        mBoardDetails.taskList[mTaskListPositon]
                            .cards[mCardPosition]
                            .assignedTo
                            .add(user.id)
                    }
                }else{
                    mBoardDetails.taskList[mTaskListPositon]
                        .cards[mCardPosition]
                        .assignedTo
                        .remove(user.id)

                    for (i in mMembersDetailList.indices){
                        if(mMembersDetailList[i].id == user.id){
                            mMembersDetailList[i].selected = false
                        }
                    }
                }
                setupSelectedMembersList()
            }
        }
        listDialog.show()
    }

    private fun updateCardDetails(){
        val card = Card(
            tv_card_title.text.toString(),
            mBoardDetails.taskList[mTaskListPositon]
                .cards[mCardPosition].createdBy,
            mBoardDetails.taskList[mTaskListPositon]
                .cards[mCardPosition].assignedTo,
            mSelectedColor,
            mSelectedDueDateMilliSeconds,
            mBoardDetails.taskList[mTaskListPositon]
                .cards[mCardPosition].toDoList

        )


        val taskList: ArrayList<Task> =mBoardDetails.taskList
        taskList.removeAt(taskList.size-1)

        mBoardDetails.taskList[mTaskListPositon].cards[mCardPosition] = card
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@CardDetailsActivity, mBoardDetails)

    }

    private fun labelColorsListDialog(){
        val colorsList:ArrayList<String> = colorsList()
        val listDialog = object: LabelColorListDialog(
            this,
            colorsList,
            resources.getString(R.string.str_select_label_color),
            mSelectedColor){
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }

        }
        listDialog.show()
    }

    private fun setupSelectedMembersList(){

        val cardAssignedMembersList =
            mBoardDetails.taskList[mTaskListPositon].cards[mCardPosition].assignedTo
        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

        for (i in mMembersDetailList.indices){
            for (j in cardAssignedMembersList){
                if(mMembersDetailList[i].id == j){
                    val selectedMember = SelectedMembers(
                        mMembersDetailList[i].id,
                        mMembersDetailList[i].image
                    )
                    selectedMembersList.add(selectedMember)
                }
            }
        }

        if (selectedMembersList.size > 0){
            selectedMembersList.add(SelectedMembers("", ""))
            tv_select_members.visibility = View.GONE
            rv_selected_members_list.visibility = View.VISIBLE
            rv_selected_members_list.layoutManager = GridLayoutManager(
                this, 6
            )
            val adapter =
                CardMemberListItemsAdapter(this, selectedMembersList, true)
            rv_selected_members_list.adapter = adapter
            adapter.setOnClickListener(
                object : CardMemberListItemsAdapter.OnClickListener{
                    override fun onClick() {
                        membersListDialog()
                    }
                }
            )
        }else{
            tv_select_members.visibility = View.VISIBLE
            rv_selected_members_list.visibility = View.GONE
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun showDatePicker(){
        val c = Calendar.getInstance()
        val year =
            c.get(Calendar.YEAR)
        val month= c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val dpd = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener{view, year, monthOfYear, dayOfMonth ->
                val sDayOfMonth = if(dayOfMonth <10) "0$dayOfMonth" else "${dayOfMonth}"
                val sMonthOfYear =
                    if ((monthOfYear +1)< 10) "0${monthOfYear +1}" else "${monthOfYear + 1}"
                val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"
                tv_select_due_date.text = selectedDate
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

                val theDate = sdf.parse(selectedDate)
                mSelectedDueDateMilliSeconds = theDate!!.time
            },
            year,
            month,
            day
        )
        dpd.show()
    }

}
