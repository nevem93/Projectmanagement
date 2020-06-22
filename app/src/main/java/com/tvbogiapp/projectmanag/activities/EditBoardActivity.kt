package com.tvbogiapp.projectmanag.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.tvbogiapp.projectmanag.R
import com.tvbogiapp.projectmanag.adapters.MembersListItemAdapterForEditBoard
import com.tvbogiapp.projectmanag.firebase.FireStoreClass
import com.tvbogiapp.projectmanag.models.Board
import com.tvbogiapp.projectmanag.models.User
import com.tvbogiapp.projectmanag.utils.Constants
import kotlinx.android.synthetic.main.activity_edit_board.*
import kotlinx.android.synthetic.main.activity_edit_board.iv_board_image

class EditBoardActivity : BaseActivity() {

    private lateinit var mBoardDetails: Board
    lateinit var mAssignedMembersList: ArrayList<User>
    private var mSelectedImageFileUri: Uri? =null
    private var mBoardImageURL: String = ""
    private var mBoardName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_board)

        getIntentData()
        setupActionBar()

        Glide
            .with(this)
            .load(mBoardDetails.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(iv_board_image)

        tv_board_title.text = mBoardDetails.name
        et_edit_board_name.setText(mBoardDetails.name)

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getAssignedMembersListDetails(this, mBoardDetails.assignedTo)

        iv_board_image.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED){
                // show image chooser
                showImageChooser()

            }else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        ib_edit_board_name.setOnClickListener {
            ll_board_title_view.visibility =View.GONE
            ll_title_edit_view.visibility = View.VISIBLE
        }

        ib_done_board_name.setOnClickListener {

            if (et_edit_board_name.text.isNotEmpty()){
                updateBoardData()
                //TODO save editBoard
            }
            else {
                Toast.makeText(this,
                    "Please enter a board name.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

        ib_close_board_name.setOnClickListener {
            et_edit_board_name.setText(mBoardDetails.name)

            ll_board_title_view.visibility =View.VISIBLE
            ll_title_edit_view.visibility = View.GONE
        }


        tv_add.setOnClickListener {
            tv_add.visibility = View.GONE
            ll_add_member_view.visibility = View.VISIBLE
        }

        ib_close_member_email.setOnClickListener {
            tv_add.visibility = View.VISIBLE
            ll_add_member_view.visibility = View.GONE
        }

        ib_done_email.setOnClickListener{
            val email = et_add_member_email.text.toString()
            if(email.isNotEmpty()){
                showProgressDialog(resources.getString(R.string.please_wait))
                FireStoreClass().getMemberDetails(this, email)
            }else{
                Toast.makeText(this,
                    "Please enter members email address.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }
    private fun uploadBoardImage(){
        showProgressDialog(resources.getString(R.string.please_wait))

        val sRef: StorageReference =
            FirebaseStorage.getInstance().reference.child(
                "BOARD_IMAGE" + System.currentTimeMillis()
                        +"."+Constants.getFileExtension(this, mSelectedImageFileUri))
        sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot ->
            Log.e(
                "Board Image URL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
            )
            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                Log.e("Downloadable Image URL", uri.toString())

                mBoardImageURL = uri.toString()
                hideProgressDialog()
                updateBoardData()
            }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        exception.message,
                        Toast.LENGTH_LONG
                    ).show()

                    hideProgressDialog()
                }
        }
    }

    fun boardDetails(board: Board) {
        mBoardDetails = board
        tv_board_title.text = mBoardDetails.name
        et_edit_board_name.setText(mBoardDetails.name)


        ll_board_title_view.visibility =View.VISIBLE
        ll_title_edit_view.visibility = View.GONE

        hideProgressDialog()
        setupActionBar()
    }

    private fun updateBoardData(){

        if(mBoardImageURL.isNotEmpty() && mBoardImageURL != mBoardDetails.image){
            val newBoardImage = mBoardImageURL
            showProgressDialog(resources.getString(R.string.please_wait))
            FireStoreClass().updateBoardImage(this, mBoardDetails, newBoardImage)

        }
        if (et_edit_board_name.text.toString() != mBoardDetails.name) {

            val newBoardName = et_edit_board_name.text.toString()
            FireStoreClass().updateBoardName(this, mBoardDetails, newBoardName)
        }
    }

    fun boardUpdatedSuccess(){
        showProgressDialog(resources.getString(R.string.please_wait))

        Glide
            .with(this)
            .load(mBoardImageURL)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(iv_board_image)

        FireStoreClass().getBoardDetails(this, mBoardDetails.documentId)


        boardDetails(mBoardDetails)



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK
            && requestCode == PICK_IMAGE_REQUEST_CODE
            && data!!.data != null){
            mSelectedImageFileUri = data.data
            uploadBoardImage()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == READ_STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty()&& grantResults[0] == PackageManager.PERMISSION_GRANTED){
                showImageChooser()
            }
        } else{
            Toast.makeText(this,
                "OOPS, you just denied the permission for storage. " +
                        "You can make changes in your settings",
                Toast.LENGTH_SHORT).show()
        }
    }
    private fun showImageChooser(){
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)

    }

    fun memberAssignSuccess(user:User){

        mAssignedMembersList.add(user)

        tv_add.visibility = View.VISIBLE
        ll_add_member_view.visibility = View.GONE
        et_add_member_email.setText(resources.getString(R.string.email))

        setupSelectedMembersList(mAssignedMembersList)
    }

    fun memberDetails(user:User){
        mBoardDetails.assignedTo.add(user.id)
        FireStoreClass().assignMemberToBoard(this, mBoardDetails, user)
    }



    fun setupSelectedMembersList(list:ArrayList<User>){
        mAssignedMembersList = list

        hideProgressDialog()

        rv_selected_members_list_edit_board.layoutManager = LinearLayoutManager(this)
        rv_selected_members_list_edit_board.setHasFixedSize(true)

        val adapter = MembersListItemAdapterForEditBoard(this, list)
        rv_selected_members_list_edit_board.adapter=adapter

    }

    fun removeMemberFromList(user:User){
        mBoardDetails.assignedTo.remove(user.id)
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().updateMemberListAfterRemoving(this, mBoardDetails)
    }

    fun removingSuccess(){
        showProgressDialog(resources.getString(R.string.please_wait))
        Toast.makeText(this, "Removing was successfully.", Toast.LENGTH_SHORT).show()
        FireStoreClass().getAssignedMembersListDetails(this, mBoardDetails.assignedTo)

    }


    private fun setupActionBar(){
        setSupportActionBar(toolbar_edit_board)

        val actionBar = supportActionBar
        if( actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = mBoardDetails.name
        }

        toolbar_edit_board.setNavigationOnClickListener {

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

    companion object{
        private const val PICK_IMAGE_REQUEST_CODE = 21
        private const val READ_STORAGE_PERMISSION_CODE = 22

    }
}
