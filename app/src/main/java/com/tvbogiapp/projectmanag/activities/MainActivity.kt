package com.tvbogiapp.projectmanag.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.tvbogiapp.projectmanag.R
import com.tvbogiapp.projectmanag.adapters.BoardItemsAdapter
import com.tvbogiapp.projectmanag.firebase.FireStoreClass
import com.tvbogiapp.projectmanag.models.Board
import com.tvbogiapp.projectmanag.models.User
import com.tvbogiapp.projectmanag.utils.Constants
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.main_content.*
import kotlinx.android.synthetic.main.nav_header_main.*


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    companion object{
        const val MY_PROFILE_REQUEST_CODE: Int = 11
        const val CREATE_BOARD_REQUEST_CODE: Int = 12
    }
    private lateinit var mUserName:String
    private lateinit var mSharedPreferences: SharedPreferences
    private lateinit var mBoardDocumentId: String
    private lateinit var mBoardsDetails:Board
    private lateinit var activeBoard: ArrayList<Board>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardsDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }
        setupActionBar()
        nav_view.setNavigationItemSelectedListener (this)

        mSharedPreferences = this.getSharedPreferences(
            Constants.PROJEMANAG_PREFERENCES, Context.MODE_PRIVATE)

        val tokenUpdated = mSharedPreferences
            .getBoolean(Constants.FCM_TOKEN_UPDATED, false)

        if(tokenUpdated){
            showProgressDialog(resources.getString(R.string.please_wait))
            FireStoreClass().loadUserData(this, true)
        }else{
            FirebaseInstanceId.getInstance()
                .instanceId.addOnSuccessListener (this@MainActivity){
                        instanceIdResult->
                    updateFCMToken(instanceIdResult.token)
                }
        }

        FireStoreClass().loadUserData(this, true)

        fb_create_board.setOnClickListener {
            val intent = Intent(this,CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }

    }

    override fun onResume() {
        FireStoreClass().getBoardList(this)
        super.onResume()}

    fun updateNavigationUserDetails(user: User, readBoardList: Boolean){
        mUserName = user.name

        val headerView = nav_view.getHeaderView(0)
        val navUserImage = headerView.findViewById<ImageView>(R.id.iv_user_image_profile)


        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(nav_user_image)

        val navUsername = headerView.findViewById<TextView>(R.id.tv_username)
        // Set the user name
        navUsername.text = user.name
        if (readBoardList){
            showProgressDialog(resources.getString(R.string.please_wait))
            FireStoreClass().getBoardList(this)
        }
    }

    fun populateBoardsListToUI(boardsList: ArrayList<Board>){

        mBoardDocumentId = Constants.DOCUMENT_ID
        activeBoard= ArrayList<Board>()
        if(boardsList.size > 0){
            rv_board_list.visibility = View.VISIBLE
            tv_no_boards_available.visibility = View.GONE

            for (board in boardsList){
                if (!board.archive){
                    activeBoard.add(board)
                }
            }

            rv_board_list.layoutManager = LinearLayoutManager(this)
            rv_board_list.setHasFixedSize(true)


            val adapter = BoardItemsAdapter(this, activeBoard)
            rv_board_list.adapter = adapter

            adapter.setOnClickListener(object: BoardItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(mBoardDocumentId, model.documentId)
                    startActivity(intent)
                }
            })

            //swipe options of tables
        }else {
            rv_board_list.visibility = View.GONE
            tv_no_boards_available.visibility = View.VISIBLE
        }
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_main_activity)
        toolbar_main_activity.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        toolbar_main_activity.setNavigationOnClickListener {
            //Toggle drawer
            toggleDrawer()
        }

    }

    private fun toggleDrawer(){
        if (drawer_layout.isDrawerOpen(GravityCompat.START)){
            drawer_layout.closeDrawer(GravityCompat.START)
        }else{
            drawer_layout.openDrawer(GravityCompat.START)
        }

    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)){
            drawer_layout.closeDrawer(GravityCompat.START)
        }else{
            doubleBackToExit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK
            && requestCode == MY_PROFILE_REQUEST_CODE){
            FireStoreClass().loadUserData(this@MainActivity)
        }else if(resultCode == Activity.RESULT_OK
            && requestCode == CREATE_BOARD_REQUEST_CODE){
            FireStoreClass().getBoardList(this@MainActivity)

        }
        else{
            Log.e("Cancelled", "Cancelled")
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.nav_my_profile ->{
                startActivityForResult(
                    Intent(this@MainActivity,
                        MyProfileActivity::class.java),
                    MY_PROFILE_REQUEST_CODE)
            }
            R.id.nav_sign_out ->{
                FirebaseAuth.getInstance().signOut()

                mSharedPreferences.edit().clear().apply()

                val intent= Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
                }
            R.id.nav_archive_boards_list->{
                startActivity(
                    Intent(this,
                        ArchiveBoardsListActivity::class.java)
                    )

            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)

        return true
    }

    fun tokenUpdateSuccess(){
        val editor :SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().loadUserData(this, true)
    }

    private fun updateFCMToken(token:String){
        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM__TOKEN] = token
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().updateUserProfileData(this, userHashMap)
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

    fun deleteBoard(boardId:String){
        FireStoreClass().deleteBoard(this, boardId)
    }

    fun updateBoardListSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        FireStoreClass().getBoardList(this)

    }

    fun actionMembers(mBoardDetails:Board){
        intent = Intent(this, MembersActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
        startActivityForResult(intent, TaskListActivity.MEMBERS_REQUEST_CODE)
    }

    fun actionEditTable(mBoardDetails:Board, mAssignedMembersDetailList: ArrayList<String>){
        intent = Intent(this, EditBoardActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
        intent.putExtra(Constants.ASSIGN_TO, mBoardDetails.assignedTo)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST, mAssignedMembersDetailList)
        startActivityForResult(intent,
            TaskListActivity.CARD_DETATILS_REQUEST_CODE_EDIT_BOARD_ACTIVITY
        )
    }

    fun actionDeleteBoard(mBoardDetails:Board){
        val boardName = mBoardDetails.name
        val boardId= mBoardDetails.documentId
        alertDialogForDeleteBoard(boardName, boardId)
    }

    fun actionArchiveTable(mBoardDetails:Board){
        mBoardDetails.archive = true
        FireStoreClass().updateBoardsListAfterArchived(this,mBoardDetails,mBoardDetails.archive)
    }

    fun updateAfterArchive(){
        setResult(Activity.RESULT_OK)
        FireStoreClass().getBoardList(this)
    }



}
