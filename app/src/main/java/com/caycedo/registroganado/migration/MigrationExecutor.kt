

package com.caycedo.registroganado.migration

import android.util.Log
import com.google.firebase.database.FirebaseDatabase

fun ejecutarMigracion() {
    val db = FirebaseDatabase.getInstance().getReference("animales")

    db.get().addOnSuccessListener { snap ->

        for (usuarioNode in snap.children) {

            val uid = usuarioNode.key ?: continue

            for (animalNode in usuarioNode.children) {

                val animalId = animalNode.key ?: continue
                val animalData = animalNode.value

                FirebaseDatabase.getInstance()
                    .getReference("animales_global")
                    .child(animalId)
                    .setValue(animalData)
            }
        }

        Log.e("MIGRACION", "✔ Migración COMPLETADA")
    }
}
