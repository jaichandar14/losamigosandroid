package com.bpmlinks.vbank.helper.viewmodel

class LocalStorage {

        companion object {
            private var localStorage: LocalStorage? = null
            fun getInstance(): LocalStorage {
                if (localStorage == null) {
                    localStorage = LocalStorage()
                }
                return localStorage as LocalStorage
            }
            var email:String? = ""
            var meetingTime:String? = ""
        }



}