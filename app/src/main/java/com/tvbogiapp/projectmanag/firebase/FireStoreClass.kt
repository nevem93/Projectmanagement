package com.tvbogiapp.projectmanag.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.tvbogiapp.projectmanag.activities.*
import com.tvbogiapp.projectmanag.models.Board

import com.tvbogiapp.projectmanag.models.User
import com.tvbogiapp.projectmanag.utils.Constants

class FireStoreClass {

    private  val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity:SignUpActivity, userInfo: User){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .set(userInfo,SetOptions.merge())
            .addOnSuccessListener {
                activity.hideProgressDialog()
                activity.userRegisteredSuccess()
            }.addOnFailureListener {
                e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,
                    "Error writing document", e)
            }
    }

    fun createBoard(activity: CreateBoardActivity, board: Board){
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Board created successfully.")

                Toast.makeText(activity,"Board created successfully.", Toast.LENGTH_LONG).show()
                activity.boardCreatedSuccessfully()
            }.addOnFailureListener {
                    e ->
            activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,
            "Error while creating a board", e)
            }
    }

    fun getBoardList(activity: Activity){
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGN_TO, getCurrentUserID())
            .get()
            .addOnSuccessListener {
                document ->
                when (activity) {
                    is MainActivity -> {
                        activity.hideProgressDialog()
                        Log.i(activity.javaClass.simpleName, document.documents.toString())
                        val boardList: ArrayList<Board> = ArrayList()
                        for (i in document.documents) {
                            val board = i.toObject(Board::class.java)!!
                            board.documentId = i.id
                            boardList.add(board)

                        }
                        activity.populateBoardsListToUI(boardList)
                    }
                    is ArchiveBoardsListActivity->{
                        activity.hideProgressDialog()
                        Log.i(activity.javaClass.simpleName, document.documents.toString())
                        val boardList: ArrayList<Board> = ArrayList()
                        for (i in document.documents) {
                            val board = i.toObject(Board::class.java)!!
                            board.documentId = i.id
                            boardList.add(board)

                        }
                        activity.populateBoardsListToUI(boardList)
                        activity.populateBoardsListToUI(boardList)
                    }
                }
            }.addOnFailureListener {
                e->
                when (activity) {
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is ArchiveBoardsListActivity -> {
                    }
                }
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }



    fun getBoardDetails(activity:Activity, documentID:String){

        when (activity) {
            is TaskListActivity -> {
                mFireStore.collection(Constants.BOARDS)
                    .document(documentID)
                    .get()
                    .addOnSuccessListener {
                            document ->
                        Log.i(activity.javaClass.simpleName, document.toString())
                        val board = document.toObject(Board::class.java)!!
                        board.documentId=document.id
                        activity.hideProgressDialog()
                        activity.boardDetails(board)
                    }.addOnFailureListener {
                            e->
                        activity.hideProgressDialog()
                        Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
                    }
            }
            is EditBoardActivity -> {
                mFireStore.collection(Constants.BOARDS)
                    .document(documentID)
                    .get()
                    .addOnSuccessListener {
                            document ->
                        Log.i(activity.javaClass.simpleName, document.toString())
                        val board = document.toObject(Board::class.java)!!
                        board.documentId=document.id
                        activity.hideProgressDialog()
                        activity.boardDetails(board)
                    }.addOnFailureListener {
                            e->
                        activity.hideProgressDialog()
                        Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
                    }
            }

        }

    }

    fun addUpdateTaskList(activity: Activity, board: Board){
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] =  board.taskList

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "TaskList updated successfully.")
                if (activity is TaskListActivity){
                    activity.hideProgressDialog()
                    activity.addUpdateTaskListSuccess()
                }
                else if(activity is CardDetailsActivity){
                    activity.hideProgressDialog()
                    activity.addUpdateTaskListSuccess()
                }
            }.addOnFailureListener {exception ->
                if (activity is TaskListActivity)
                    activity.hideProgressDialog()
                else if(activity is CardDetailsActivity)
                    activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating taskList", exception)

            }
    }

    fun updateUserProfileData(activity: Activity,
                       userHashMap: HashMap<String, Any>){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Profile Data updated successfully!")
                Toast.makeText(activity, "Profile updated successfully!", Toast.LENGTH_LONG).show()
                when(activity){
                    is MainActivity ->{
                        activity.hideProgressDialog()
                        activity.tokenUpdateSuccess()}
                    is MyProfileActivity ->{
                        activity.hideProgressDialog()
                        activity.profileUpdateSuccess()
                    }
                }
            }.addOnFailureListener {
                e ->
                when(activity) {
                    is MainActivity ->
                        activity.hideProgressDialog()
                    is MyProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
                Toast.makeText(activity, "Error when updating the profile!", Toast.LENGTH_LONG).show()
            }
    }

    fun loadUserData(activity: Activity, readBoardsList: Boolean = false) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)!!

                when (activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                        activity.signInSuccess(loggedInUser)
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                        activity.updateNavigationUserDetails(loggedInUser, readBoardsList)
                    }
                    is MyProfileActivity ->{
                        activity.hideProgressDialog()
                        activity.setUserDataInUI(loggedInUser)
                    }
                }

            }.addOnFailureListener {

                    e ->
                when (activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(
                    "SignInUser",
                    "Error writing document", e
                )
            }
    }

    fun getCurrentUserID():String{

        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID=""
        if (currentUser!=null){
            currentUserID = currentUser.uid
        }
        return currentUserID
    }


    fun getAssignedMembersListDetails(
        activity: Activity, assignedTo: ArrayList<String>){

        mFireStore.collection(Constants.USERS)
            .whereIn(Constants.ID, assignedTo)
            .get()
            .addOnSuccessListener {

                document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())

                val usersList: ArrayList<User> = ArrayList()

                for (i in document.documents){
                    val user = i.toObject(User::class.java)!!
                    usersList.add(user)
                }
                when (activity) {
                    is MembersActivity -> {
                        activity.hideProgressDialog()
                        activity.setupMembersList(usersList)}
                    is TaskListActivity ->{
                        activity.hideProgressDialog()
                        activity.boardMembersDetailsList(usersList)}
                    is EditBoardActivity ->{
                        activity.hideProgressDialog()
                        activity.setupSelectedMembersList(usersList)}
                }


            }.addOnFailureListener { e ->
                when (activity) {
                    is MembersActivity -> {
                        activity.hideProgressDialog()}
                    is TaskListActivity -> {
                        activity.hideProgressDialog()}
                    is EditBoardActivity -> {
                        activity.hideProgressDialog()}
                }
                Log.e(
                    activity.javaClass.simpleName, "Error while creating board",e
                )
            }
    }



    fun getMemberDetails(activity:Activity, email: String){
        mFireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener {
                document ->
                if(document.documents.size > 0){
                    if (activity is MembersActivity) {
                        activity.hideProgressDialog()
                        val user = document.documents[0].toObject(User::class.java)!!
                        activity.memberDetails(user)}
                    if(activity is EditBoardActivity){
                        activity.hideProgressDialog()
                        val user = document.documents[0].toObject(User::class.java)!!
                        activity.memberDetails(user)
                    }
                }else{
                    if (activity is MembersActivity) {
                        activity.hideProgressDialog()
                        activity.showErrorSnackBar("No such member found")
                   } else if (activity is EditBoardActivity) {
                        activity.hideProgressDialog()
                        activity.showErrorSnackBar("No such member found")
                    }
                }
            }.addOnFailureListener {e ->
                if (activity is MembersActivity)
                    activity.hideProgressDialog()
                else if (activity is EditBoardActivity)
                    activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,
                "Error while getting user details.", e)
            }
    }

    fun assignMemberToBoard(
        activity: Activity, board: Board, user: User){
        val assignedToHashmap = HashMap<String, Any>()
        assignedToHashmap[Constants.ASSIGN_TO] = board.assignedTo

        if (activity is MembersActivity) {
            mFireStore.collection(Constants.BOARDS)
                .document(board.documentId)
                .update(assignedToHashmap)
                .addOnSuccessListener {
                    activity.hideProgressDialog()
                    activity.memberAssignSuccess(user)
                }.addOnFailureListener { e ->
                    activity.hideProgressDialog()
                    Log.e(
                        javaClass.simpleName,
                        "Error while creating a board.", e
                    )
                }
        }
        if (activity is EditBoardActivity) {
            mFireStore.collection(Constants.BOARDS)
                .document(board.documentId)
                .update(assignedToHashmap)
                .addOnSuccessListener {
                    activity.hideProgressDialog()
                    activity.memberAssignSuccess(user)
                }.addOnFailureListener { e ->
                    activity.hideProgressDialog()
                    Log.e(
                        javaClass.simpleName,
                        "Error while creating a board.", e
                    )
                }
        }
    }


    fun updateMemberListAfterRemoving(activity: EditBoardActivity, board: Board) {

        val memberListHashMap = HashMap<String, Any>()
        memberListHashMap[Constants.ASSIGN_TO] =  board.assignedTo
        mFireStore.collection(Constants.BOARDS)
                .document(board.documentId)
                .update(memberListHashMap)
                .addOnCompleteListener {
                    activity.hideProgressDialog()
                    activity.removingSuccess()

        }.addOnFailureListener { e ->
            activity.hideProgressDialog()
            Log.e(
                javaClass.simpleName,
                "Error while deleting a member.", e
            )
        }

    }

    fun deleteBoard(activity: Activity, documentId:String){



        mFireStore.collection(Constants.BOARDS)
            .document(documentId)
            .delete()
            .addOnSuccessListener {
                when(activity){
                    is MainActivity->{
                        activity.hideProgressDialog()
                        activity.updateDeletedBoardList()

                        Log.d(TAG, "DocumentSnapshot successfully deleted!")}
                    is ArchiveBoardsListActivity ->{
                        activity.hideProgressDialog()
                        activity.updateDeletedBoardList()
                    }
                }
            }
            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
    }

    fun updateBoardsListAfterArchived(activity: Activity,
                                 board: Board,
                                 archive:Boolean){
        val boardHashMap = HashMap<String, Any>()
        boardHashMap[Constants.ARCHIVE] =  archive

        mFireStore.collection(Constants.BOARDS) // Collection Name
            .document(board.documentId) // Document ID
            .update(boardHashMap)// A hashmap of fields which are to be updated.
            .addOnSuccessListener {
                when (activity){

                    is MainActivity ->{

                        activity.hideProgressDialog()
                        // Profile data is updated successfully.

                        Log.e(activity.javaClass.simpleName, "Board Data updated successfully!")

                        Toast.makeText(activity, "Board updated successfully!", Toast.LENGTH_SHORT).show()

                        // Notify the success result.
                        activity.updateAfterArchive()}

                    is ArchiveBoardsListActivity ->{
                        activity.hideProgressDialog()
                        Log.e(activity.javaClass.simpleName, "Board Data updated successfully!")

                        Toast.makeText(activity, "Board updated successfully!", Toast.LENGTH_SHORT).show()
                        activity.updateAfterActive()
                    }
                }

            }
            .addOnFailureListener { e ->
                when (activity){
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(
                    activity.javaClass.simpleName,
                    "Error while updating a board.",
                    e)
            }

    }

    fun updateBoardImage(activity: EditBoardActivity,
                        board: Board, newBoardImage: String){

        val boardHashMap = HashMap<String, Any>()
        boardHashMap[Constants.IMAGE] =  newBoardImage

        mFireStore.collection(Constants.BOARDS) // Collection Name
            .document(board.documentId) // Document ID
            .update(boardHashMap)// A hashmap of fields which are to be updated.
            .addOnSuccessListener {
                // Profile data is updated successfully.
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Board Data updated successfully!")

                Toast.makeText(activity, "Board updated successfully!", Toast.LENGTH_SHORT).show()

                // Notify the success result.
                activity.boardUpdatedSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while updating a board.",
                    e)
            }
    }

    fun updateBoardName(activity: EditBoardActivity,
                        board: Board, newBoardName:String){

        val boardHashMap = HashMap<String, Any>()
        boardHashMap[Constants.NAME] = newBoardName

        mFireStore.collection(Constants.BOARDS) // Collection Name
            .document(board.documentId) // Document ID
            .update(boardHashMap)// A hashmap of fields which are to be updated.
            .addOnSuccessListener {
                activity.hideProgressDialog()
                // Profile data is updated successfully.
                Log.e(activity.javaClass.simpleName, "Board Data updated successfully!")

                Toast.makeText(activity, "Board updated successfully!", Toast.LENGTH_SHORT).show()

                // Notify the success result.
                activity.boardUpdatedSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while updating a board.",
                    e)
            }
    }


    fun updateBoardList(activity: Activity, board: Board){

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .get()
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "TaskList updated successfully.")
                when (activity) {
                    is TaskListActivity -> {
                        activity.hideProgressDialog()
                        activity.updateBoardListSuccess()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                        activity.updateBoardListSuccess()
                    }
                    is EditBoardActivity -> {
                        activity.hideProgressDialog()
                        activity.boardUpdatedSuccess()
                    }
                }
            }.addOnFailureListener {exception ->
                when (activity) {
                    is TaskListActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is EditBoardActivity ->{
                        activity.hideProgressDialog()
                    }
                }
                Log.e(activity.javaClass.simpleName, "Error while creating taskList", exception)
            }
    }

    companion object{
        const val TAG ="FIRESTORECLASS: "
    }

}