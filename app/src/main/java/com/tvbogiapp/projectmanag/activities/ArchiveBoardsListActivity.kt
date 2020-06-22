package com.tvbogiapp.projectmanag.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.tvbogiapp.projectmanag.R
import com.tvbogiapp.projectmanag.adapters.ArchiveBoardItemsAdapterprivate
import com.tvbogiapp.projectmanag.adapters.BoardItemsAdapter
import com.tvbogiapp.projectmanag.firebase.FireStoreClass
import com.tvbogiapp.projectmanag.models.Board
import com.tvbogiapp.projectmanag.models.User
import com.tvbogiapp.projectmanag.utils.Constants
import kotlinx.android.synthetic.main.activity_archive_boards_list.*
import kotlinx.android.synthetic.main.activity_archive_boards_list.tv_no_boards_available

class ArchiveBoardsListActivity : BaseActivity() {

    private lateinit var mBoardDetails: Board
    lateinit var mAssignedMembersList: ArrayList<User>
    private lateinit var mBoardDocumentId: String
    private lateinit var archiveBoardsList: ArrayList<Board>

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_archive_boards_list)
        getIntentData()
        setupActionBar()

    }
    override fun onResume() {
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getBoardList(this)
        super.onResume()}

    fun populateBoardsListToUI(boardsList: ArrayList<Board>){

        mBoardDocumentId = Constants.DOCUMENT_ID
        archiveBoardsList = ArrayList<Board>()

        for (board in boardsList){
            if (board.archive){
                archiveBoardsList.add(board)
            }
        }


        if(boardsList.size > 0){
            rv_archive_board_list.visibility = View.VISIBLE
            tv_no_boards_available.visibility = View.GONE

            rv_archive_board_list.layoutManager = LinearLayoutManager(this)
            rv_archive_board_list.setHasFixedSize(true)

            val adapter = ArchiveBoardItemsAdapterprivate(this, archiveBoardsList)
            rv_archive_board_list.adapter = adapter


        }else {
            rv_archive_board_list.visibility = View.GONE
            tv_no_boards_available.visibility = View.VISIBLE
        }
    }

    fun actionDeleteBoard(mBoardDetails:Board){
        val boardName = mBoardDetails.name
        val boardId= mBoardDetails.documentId
        alertDialogForDeleteBoard(boardName, boardId)
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_archive_board)

        val actionBar = supportActionBar
        if( actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = "Archive Boards List"
        }


        toolbar_archive_board.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    private fun getIntentData(){

        if(intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL)!!
            FireStoreClass().getAssignedMembersListDetails(this, mBoardDetails.assignedTo)
        }
        if(intent.hasExtra(Constants.BOARD_MEMBERS_LIST)){
            mAssignedMembersList = intent.getParcelableArrayListExtra(
                Constants.BOARD_MEMBERS_LIST)!!
        }
    }

    fun activateBoard(mBoardDetails:Board){
        mBoardDetails.archive = false
        FireStoreClass().updateBoardsListAfterArchived(this,mBoardDetails,mBoardDetails.archive)
    }

    fun updateAfterActive(){
        setResult(Activity.RESULT_OK)
        FireStoreClass().getBoardList(this)
    }

    private fun deleteBoard(boardId:String){
        FireStoreClass().deleteBoard(this, boardId)
    }

    fun updateDeletedBoardList(){
        showProgressDialog(resources.getString(R.string.please_wait))
        setResult(Activity.RESULT_OK)
        FireStoreClass().getBoardList(this)
    }

    private fun alertDialogForDeleteBoard(boardName: String, boardId: String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.alert))
        builder.setMessage(
            resources.getString(
                R.string.confirmation_message_to_delete_board, boardName
            )
        )
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton(resources.getString(R.string.yes)){dialogInterface, _ ->
            dialogInterface.dismiss()
            showProgressDialog(resources.getString(R.string.please_wait))
            deleteBoard(boardId)
            startActivityForResult(intent, RESULT_OK)

        }
        builder.setNegativeButton(resources.getString(R.string.no)){dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}