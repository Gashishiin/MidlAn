package ru.skillbranch.kotlinexample

object UserHolder {
    private val map = mutableMapOf<String, User>()

    fun registerUser(
        fullName: String,
        email: String,
        password: String
    ): User {
        return User.makeUser(fullName, email = email, password = password)
            .also {
                if (map[it.login] != null) throw IllegalArgumentException("A user with this email already exists")
                else map[it.login] = it
            }
    }

    fun registerUserByPhone (
        fullName: String,
        rawPhone:  String
    ):User {
        return User.makeUser(fullName, phone = rawPhone)
            .also {
                user -> if (map[user.login] != null) throw IllegalArgumentException("A user with this phone already exists")
                else map[user.login] = user
            }
    }

    fun loginUser(login: String, password: String): String? {
        val user = map[login.trim()] ?: map[login.replace("[^+\\d]".toRegex(), "")]
        return user?.run {
            if (checkPassword(password)) this.userInfo
            else null
        }
    }

    fun requestAccessCode(login: String) {
        map[login.replace("[^+\\d]".toRegex(), "")]?.requestAccessCode()
    }

    fun importUsers(list: List<String>): List<User> {
        return list.map {
            val data = it.split(";")
            val salt = data[2].split(":")[0]
            val hash = data[2].split(":")[1]
            val user = User.importFromCsv(
                data[0],
                if(data[1].isBlank()) null else data[1],
                salt,
                hash,
                if(data[3].isBlank()) null else data[3]
            )
            map[user.login] = user
            user
        }

    }

    fun clear() {
        map.clear()
    }

}