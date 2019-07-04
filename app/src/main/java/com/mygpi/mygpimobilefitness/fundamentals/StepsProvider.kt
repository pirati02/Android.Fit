package com.mygpi.mygpimobilefitness.fundamentals

import android.annotation.SuppressLint
import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.mygpi.mygpimobilefitness.fundamentals.StepsProvider.StepColumns.ALL_STEPS
import com.mygpi.mygpimobilefitness.fundamentals.StepsProvider.StepColumns.STEPS_BETWEEN

import com.mygpi.mygpimobilefitness.model.StepModel

import io.realm.Realm
import io.realm.RealmResults
import java.text.SimpleDateFormat
import java.util.*


class StepsProvider : ContentProvider() {

    override fun getType(uri: Uri): String? {
        when (uriMatcher.match(uri)) {
            ALL_STEPS -> return "vnd.android.cursor.dir/vnd.example.all_steps"
            else -> throw IllegalArgumentException("Unsupported URI: $uri")
        }
    }

    override fun onCreate(): Boolean {
        return true
    }

    @SuppressLint("SimpleDateFormat")
    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val realm = Realm.getDefaultInstance()
        val match = uriMatcher.match(uri)
        val myCursor = MatrixCursor(arrayOf("numSteps"))
        realm.use {
            when (match) {
                ALL_STEPS -> {
                    var realmTask: RealmResults<StepModel>? = null
                    if (uri.pathSegments?.size!! > 0) {
                        val startDate = Calendar.getInstance().apply {
                            time = SimpleDateFormat("dd-mm-yyyy").parse(uri.pathSegments[1])
                        }
                        val endDate = Calendar.getInstance().apply {
                            time = SimpleDateFormat("dd-mm-yyyy").parse(uri.pathSegments[2])
                        }
                        realmTask = if (startDate != null || endDate != null)
                            it.where(StepModel::class.java).between("date", startDate.time, endDate.time).findAll()
                        else
                            it.where(StepModel::class.java).findAll()

                        for (result in realmTask) {
                            val rowData = arrayOf<Any>(result.numSteps)
                            myCursor.addRow(rowData)
                        }
                    }
                }
                else -> throw UnsupportedOperationException("Unknown uri: $uri")
            }
            myCursor.setNotificationUri(context?.contentResolver, uri)
        }
        return myCursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                        selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    object StepColumns {
        val ALL_STEPS = -1
        val STEPS_BETWEEN = 2
    }

    companion object {
        val PROVIDER_NAME = "com.mygpi.mygpimobilefitness.StepsProvider"
        val URL = "content://$PROVIDER_NAME/students"
        val CONTENT_AUTHORITY = Uri.parse(URL)

        internal val uriCode = 1
        internal val uriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        init {
            uriMatcher.addURI(PROVIDER_NAME, "steps", uriCode)
            uriMatcher.addURI(PROVIDER_NAME, "steps/#", uriCode)
        }
    }
}