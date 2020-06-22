package com.tvbogiapp.projectmanag.models

import android.os.Parcel
import android.os.Parcelable

data class Card (
    var name: String= "",
    val createdBy: String = "",
    val assignedTo: ArrayList<String> = ArrayList(),
    val labelColor: String = "",
    val dueDate: Long = 0,
    val toDoList: ArrayList<ToDoList> = ArrayList()
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.createTypedArrayList(ToDoList.CREATOR)!!
    )

    override fun writeToParcel(dest: Parcel, flags: Int) =with(dest) {
        writeString(name)
        writeString(createdBy)
        writeStringList(assignedTo)
        writeString(labelColor)
        writeLong(dueDate)
        writeTypedList(toDoList)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Card> {
        override fun createFromParcel(parcel: Parcel): Card {
            return Card(parcel)
        }

        override fun newArray(size: Int): Array<Card?> {
            return arrayOfNulls(size)
        }
    }
}