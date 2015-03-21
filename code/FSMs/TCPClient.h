#ifndef __TCPCLIENT_h__
#define __TCPCLIENT_h__

#include <string>

class TCPClient
{
public:
  TCPClient();
  ~TCPClient();

  void Send(const std::string& message);
private:
  int m_sd;
  char m_Buffer[256];
  bool m_Connected;
};

#endif // __TCPCLIENT_h__
