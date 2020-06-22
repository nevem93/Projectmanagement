package com.tvbogiapp.projectmanag.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.tvbogiapp.projectmanag.R
import com.tvbogiapp.projectmanag.adapters.TaskListItemAdapter
import com.tvbogiapp.projectmanag.firebase.FireStoreClass
import com.tvbogiapp.projectmanag.models.*
import com.tvbogiapp.projectmanag.utils.Constants
import kotlinx.android.synthetic.main.activity_task_list.*

class TaskListActivity : BaseActivity() {

    private lateinit var mBoardDetails: Board
    private lateinit var mBoardDocumentId: String
    lateinit var mAssignedMembersDetailList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        if (intent.hasExtra(Constants.DOCUMENT_ID)){
            mBoardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }
    }
    fun boardDetails(board: Board) {
        mBoardDetails = board
        setupActionBar()

        rv_task_list.visibility = View.VISIBLE

        val adapter = TaskListItemAdapter(this, mBoardDetails.taskList)
        rv_task_list.adapter = adapter


        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getAssignedMembersListDetails(this,
            mBoardDetails.assignedTo)
    }

    override fun onResume() {
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getBoardDetails(this,mBoardDocumentId)
        super.onResume()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK &&
            requestCode == MEMBERS_REQUEST_CODE ||
            requestCode == CARD_DETATILS_REQUEST_CODE ||
            requestCode == DELETE_BOARD_REQUEST_CODE){
            showProgressDialog(resources.getString(R.string.please_wait))
            FireStoreClass().getBoardDetails(this,mBoardDocumentId)
        } else{
            Log.e("CANCELLED", "cancelled")
        }
    }

    fun cardDetails(taskListPosition: Int, cardPosition: Int){
        intent = Intent(this, CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION, taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION, cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST, mAssignedMembersDetailList)
        startActivityForResult(intent, CARD_DETATILS_REQUEST_CODE)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_members -> {
                intent = Intent(this, MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
                startActivityForResult(intent, MEMBERS_REQUEST_CODE)
                return true
            }
            R.id.action_edit_table -> {
                intent = Intent(this, EditBoardActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
                intent.putExtra(Constants.ASSIGN_TO, mBoardDetails.assignedTo)
                intent.putExtra(Constants.BOARD_MEMBERS_LIST, mAssignedMembersDetailList)
                startActivityForResult(intent, CARD_DETATILS_REQUEST_CODE_EDIT_BOARD_ACTIVITY)

            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun setupActionBar(){
        hideProgressDialog()
        setSupportActionBar(toolbar_task_list_activity)

        val actionBar = supportActionBar
        if( actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = mBoardDetails.name
        }

        toolbar_task_list_activity.setNavigationOnClickListener {
            onBackPressed()
        }

    }

    fun addUpdateTaskListSuccess(){
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getBoardDetails(this@TaskListActivity, mBoardDetails.documentId)
    }

    fun createTaskList(taskListName: String){
        Log.e("Task List Name", taskListName)
        val task = Task(taskListName, FireStoreClass().getCurrentUserID())
        mBoardDetails.taskList.add(0,task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size -1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@TaskListActivity,mBoardDetails)
    }

    fun updateTaskList(position:Int, listName: String, model: Task){
        val task = Task(listName, model.createdBy, model.cards)

        mBoardDetails.taskList[position] = task
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@TaskListActivity,mBoardDetails)
    }

    fun deleteTaskList(position: Int){
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@TaskListActivity,mBoardDetails)
    }

    fun addCardToTaskList(position: Int, cardName: String){
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(FireStoreClass().getCurrentUserID())

        val card = Card(cardName, FireStoreClass().getCurrentUserID(), cardAssignedUsersList)

        val cardsList = mBoardDetails.taskList[position].cards
        cardsList.add(card)

        val task = Task (
            mBoardDetails.taskList[position].title,
            mBoardDetails.taskList[position].createdBy,
            cardsList
        )
        mBoardDetails.taskList[position] = task

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@TaskListActivity,mBoardDetails)

    }

    fun boardMembersDetailsList(list:ArrayList<User>){
        hideProgressDialog()
        mAssignedMembersDetailList = list

        mBoardDetails.taskList

        rv_task_list.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL,false)
        rv_task_list.setHasFixedSize(true)


      /*  val addTaskList = Task(resources.getString(R.string.add_list))
        mBoardDetails.taskList.add(addTaskList)

       */


        /*rv_task_list.layoutManager = LinearLayoutManager(
            this,LinearLayoutManager.HORIZONTAL,false)
        rv_task_list.setHasFixedSize(true)
        val adapter = TaskListItemAdapter(this@TaskListActivity, mBoardDetails.taskList)
        rv_task_list.adapter = adapter

         */
        
    }

    fun updateCardsInTaskList(taskListPosition: Int, cards:ArrayList<Card>){
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        mBoardDetails.taskList[taskListPosition].cards = cards

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@TaskListActivity,mBoardDetails)
    }

    private fun alertDialogForDeleteBoard(boardName: String, boardId: String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.alert))
        builder.setMessage(
            resources.getString(
                R.string.confirmation_message_to_delete_board,
                boardName
            )
        )
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton(resources.getString(R.string.yes)){dialogInterface, _ ->
            dialogInterface.dismiss()
            showProgressDialog(resources.getString(R.string.please_wait))
            MainActivity().deleteBoard(boardId)
        }
        builder.setNegativeButton(resources.getString(R.string.no)){dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }


    fun updateBoardListSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    companion object{
        const val  MEMBERS_REQUEST_CODE: Int = 13
        const val CARD_DETATILS_REQUEST_CODE: Int = 14
        const val DELETE_BOARD_REQUEST_CODE: Int = 15
        const val CARD_DETATILS_REQUEST_CODE_EDIT_BOARD_ACTIVITY: Int =16
    }
}
