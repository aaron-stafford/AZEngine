#include "TCPClient.h"
#include <iostream>
#include <string>
#include <string.h>
#include <errno.h>

//Network related includes:
#include <sys/socket.h>
#include <netdb.h>
#include <arpa/inet.h>

//Target host details:
#define PORT 7777
#define HOST "127.0.0.1"
//#define HOST "localhost"

TCPClient::TCPClient(): m_Connected(false)
{
  int ret;
  struct sockaddr_in server;
  struct in_addr ipv4addr;
  struct hostent *hp;

  m_sd = socket(AF_INET,SOCK_STREAM,0);

  if(m_sd < 0)
  {
    std::cout << "failed to acquire socket descriptor" << std::endl;
  }
  else
  {
    std::cout << "socket descriptor acquired: " << m_sd << std::endl;
  }
  
  inet_pton(AF_INET, HOST, &ipv4addr);
  
  hp = gethostbyaddr(&ipv4addr, sizeof ipv4addr, AF_INET);

  if(hp == NULL)
  {
    std::cout << "failed to host by address" << std::endl;
  }
  else
  {
    std::cout << "Got host by address" << std::endl;
  }
  
  server.sin_family = AF_INET;
  server.sin_port = htons(PORT);
  bcopy(hp->h_addr, &(server.sin_addr.s_addr), hp->h_length);
  
  std::cout << "Attempting tcp connection to server" << std::endl;

  ret = connect(m_sd, (const sockaddr *)&server, sizeof(server));

  if(ret==0)
  {
    std::cout << "TCP client conencted successfully" << std::endl;
    m_Connected = true;
  }
  else
  {
    std::cout << "TCP client failed to connect to server" << std::endl;
    std::cout << "Reason: " << strerror(errno) << std::endl;
  }
}

void TCPClient::Send(const std::string& message)
{
  if(!m_Connected)
  {
    std::cout << "Not sending message. Not connected" << std::endl;
    return;
  }

  std::cout << "Sending message: " << message << std::endl;
  ssize_t bytesSent = send(m_sd, (char *)message.c_str(), strlen((char *)message.c_str()), 0);

  if(bytesSent == -1)
  {
    std::cout << "failed to send message" << std::endl;
    std::cout << "Reason: " << strerror(errno) << std::endl;
  }
  else
  {
    std::cout << "Sent: " << bytesSent << " byte(s) to server" << std::endl;
  }

  bzero(m_Buffer, 256);
  ssize_t n = read(m_sd, m_Buffer, 255);
  if (n < 0)
  {
    std::cout << "failed to read from socket" << std::endl;
    std::cout << "Reason: " << strerror(errno) << std::endl;
  }
  else
  {
    std::cout << "Response from server: " << m_Buffer <<  std::endl;
  }
}

TCPClient::~TCPClient()
{
  if(m_Connected)
  {
    int result = shutdown(m_sd, 2);
    if(result !=0)
    {
      std::cout << "Failed to close socket. Reason: " << strerror(errno) << std::endl;
    }
  }
}
