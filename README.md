# Atividade-Redes-Sockets



### Cliente do servidor de ECHO.
    Uso: java EchoClient [host] [porta]   (padrão: localhost 4444)

 Digite comandos, por exemplo:
   ECHO ola mundo
   QUIT
 

 ### Servidor de ECHO multi-threaded.
    Uso: java EchoServer [porta] [threads]   (padrão: 4444 10)
 
    Os clientes são atendidos por um pool fixo de threads (ExecutorService): as threads são criadas uma vez e reaproveitadas entre conexões.
 
    Protocolo (uma mensagem por linha):
    Cliente -> Servidor          Servidor -> Cliente
    ECHO <mensagem>              OK <mensagem>
    QUIT                         BYE   (e fecha a conexão)
    qualquer outra coisa         ERR <descrição>
