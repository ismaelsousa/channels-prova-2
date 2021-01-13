package org.example

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce

data class Torcedor(val time:String)
class Onibus(val time:String,  val tocedores:MutableList<Torcedor>){
    var scopedapartida:Job? = null
    var jaPartiu = false
   suspend fun sair()= runBlocking {
       if(tocedores.size==1){
          scopedapartida =  GlobalScope.launch {
               partida()
           }
       }
        if(tocedores.size==5){
            jaPartiu = true
            scopedapartida?.cancelChildren()
            scopedapartida?.job?.cancelChildren()
            println("------------------------------------------------------- CHEIO")
            println("onibus "+time+" precisa sair esta lotado!! \ntocedores $tocedores")
            println("------------------------------------------------------- CHEIO")


            this.coroutineContext.cancel()
        }
    }

    suspend fun partida() {
        delay(10000)
        if(jaPartiu) {
            jaPartiu = false
            return
        }
        println("------------------------------------------------------- PARTIDA")
        if(tocedores.size<5)  println("onibus "+time+" saiu sem está cheio \ntocedores $tocedores") else println("onibus "+time+" saiu")
        println("------------------------------------------------------- PARTIDA")

    }
}


fun main() = runBlocking{
    val channelTorcedor = Channel<Torcedor>()
    val onibus1channel = Channel<Torcedor>()
    val onibus2channel = Channel<Torcedor>()



    val oni1 = Onibus("fortaleza",mutableListOf<Torcedor>())
    val oni2 = Onibus("ceara", mutableListOf<Torcedor>())

    launch {
        println("iniciou a ouvir onibus1")
        for (tocedor in onibus1channel){
            oni1.tocedores.add(tocedor)
            oni1.sair()
        }
    }
    launch {
        println("iniciou a ouvir onibus2")
        for (tocedor in onibus2channel){
            oni2.tocedores.add(tocedor)
            oni2.sair()
        }
    }





   launch {
       // Criar e enviar tocedor para amc
       for (i in 1..9){
           if (i%2==0){
                channelTorcedor.send(Torcedor("fortaleza"))
           }else{
                channelTorcedor.send(Torcedor("ceara"))
           }
       }

   }

    fun amc(channelTorcedor: ReceiveChannel<Torcedor>, onibus1:Channel<Torcedor>, onibus2:Channel<Torcedor>) = produce<Torcedor> {
        for(torcedor in channelTorcedor){
            if(torcedor.time=="fortaleza"){
                println("1 torcedor do fortaleza")
                onibus1.send(torcedor)
            }else{
                println("1 torcedor do ceara")
                onibus2.send(torcedor)
            }
        }
    }


    GlobalScope.launch {
        runBlocking {
              withTimeout(60){
                  delay(100)
                 println("Iniciou time out ${this.coroutineContext}")
            }
            println("time out ")
        }
    }

    val Amc = amc(channelTorcedor, onibus1 = onibus1channel, onibus2 = onibus2channel)


    // Criar AMC para ouvir pedidos
    // Criar dois onibusc que recebem pedidos das AMC
    // Criar torcedores que pedem passagem
    // AMC Envia um onibus pro torcedor
    // Onibus inicia tempo para saida

}

