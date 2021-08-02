using RabbitMQ.Client;
using RabbitMQ.Client.Events;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;

namespace Klient
{
    public class KlientRabbit
    {
        /// <summary>
        /// Pole przechowujące nazwę klienta
        /// </summary
        private string _nazwa;

        /// <summary>
        /// Pole przechowujące konfiguracje
        /// </summary
        private Konfiguracja _config;

        /// <summary>
        /// Pole przechowujące stan zadania
        /// </summary
        private StanZadania stanZadania = StanZadania.NieUruchomione;

        /// <summary>
        /// Pole przechowujące aktualnie wykonywane zadanie
        /// </summary
        private string zadanie = "Brak Zadania";

        /// <summary>
        /// Flaga zatrzymania pracy klienta
        /// </summary
        private bool flagStop = true;

        /// <summary>
        /// Flaga działania klienta
        /// </summary
        private bool flagIsRuning = false;

        /// <summary>
        /// Semafor czekający na zakończenie pracy klienta
        /// </summary
        protected volatile AutoResetEvent semaphore = new AutoResetEvent(true);

        /// <summary>
        /// Semafor pilnujacy czy wykonywane jest zadanie
        /// </summary
        protected volatile AutoResetEvent semaphore2 = new AutoResetEvent(true);

        /// <summary>
        /// Pole przehowujące wątek, który w tle jest połączony z serwisem
        /// </summary
        private Thread thread=null;

        public KlientRabbit(string i_nazwa, Konfiguracja i_konfiguracja)
        {
            this._nazwa = i_nazwa;
            this._config = i_konfiguracja;
        }

        /// <summary>
        /// Getter pobierający wartość stanu zadania
        /// </summary
        public StanZadania getStan()
        {
            return this.stanZadania;
        }

        /// <summary>
        /// Getter pobierający nazwe klienta
        /// </summary
        public string getNazwa()
        {
            return this._nazwa;
        }

        /// <summary>
        /// Getter pobierający wartość flagi informującej o uruchomieniu
        /// </summary
        public bool getStart()
        {
            return flagIsRuning;
        }

        /// <summary>
        /// Getter pobierający wartość flagi informującej o zatrzymaniu
        /// </summary
        public bool getStop()
        {
            return flagStop;
        }

        /// <summary>
        /// Getter pobierający aktualnie wykonywane zadanie
        /// </summary
        public string getZadanie()
        {
            return zadanie;
        }

        /// <summary>
        /// Metoda uruchamiająca prace klienta
        /// </summary
        public void KlientStart()
        {
            if (!flagIsRuning)
            {
                thread = new Thread(() => CreateClient());
                thread.Start();
                stanZadania = StanZadania.Czeka;
                string txtLog = "[" + DateTime.Now.ToString() + "] Uruchomienie klienta";
                WriteInFile(_nazwa.ToString(), txtLog);
                flagStop = false;
            }
        }

        /// <summary>
        /// Metoda konfigurująca dane połączeniowe
        /// </summary
        private void KonfigurujPolaczenie(ref ConnectionFactory factory)
        {

            if (!(_config.RabitMQ_UserName.Equals(""))) factory.UserName = _config.RabitMQ_UserName;
            if (!_config.RabitMQ_Password.Equals("")) factory.Password = _config.RabitMQ_Password;
            if (!(_config.RabitMQ_VirualHost.Equals("")))factory.VirtualHost = _config.RabitMQ_VirualHost;
            if (!(_config.RabitMQ_Port==0)) factory.Port = _config.RabitMQ_Port;
            factory.HostName = _config.RabitMQ_HostName;
        }

        /// <summary>
        /// Metoda tworzaca polaczenie miedzy klientam a serwisem
        /// </summary
        public void CreateClient()
        {
            var factory = new ConnectionFactory();
            try
            {
                KonfigurujPolaczenie(ref factory);
                flagIsRuning = true;
                using (var connection = factory.CreateConnection())
                {
                    using (var channel = connection.CreateModel())
                    {
                        //Ustawienie maksymalnej ilości przerabianych wiadomości
                        channel.BasicQos(prefetchSize: 0, prefetchCount: 1, global: true);
                        var consumer = new EventingBasicConsumer(channel);
                        consumer.Received += Consumer_Received;
                        for(int i=0; i < _config.RabitMQ_ilKolejek; i++)
                        {
                            channel.BasicConsume(queue: _config.RabitMQ_QueueRecive.ElementAt(i), autoAck: false, consumer: consumer);
                        }
                        //channel.BasicConsume(queue: _config.RabitMQ_QueueRecive, autoAck: false, consumer: consumer);
                        //channel.BasicConsume(queue: "tasks2", autoAck: false, consumer: consumer);
                        //Wykonuj dopóki flaga zatrzymanai nie zostanie podniesiona
                        while (!flagStop)
                        {
                            semaphore.WaitOne(-1, true);
                            if (flagStop) break;
                        }
                        stanZadania = StanZadania.Koniec;
                        flagIsRuning = false;
                    }

                }
            }
            catch(ThreadInterruptedException ex)
            {
                Debug.WriteLine("Przewanie watku");
            }
            catch(RabbitMQ.Client.Exceptions.BrokerUnreachableException ex)
            {
                MessageBox.Show("Błąd połączenia\nHostname: " + factory.HostName + "\nUserName:" + factory.UserName + "\nPass:" + factory.Password + "\nVhost:" + _config.RabitMQ_VirualHost+"\n"+ex.Message);
            }
            catch(Exception e)
            {
                MessageBox.Show(e.Message);
            }
        }

        /// <summary>
        /// Uruchomienie procesu zatrzymywania klienta
        /// </summary
        public void KlientStop()
        {
            flagStop = true;
            semaphore2.WaitOne(-1, true);
            semaphore.Set();
            semaphore2.Set();
        }

        /// <summary>
        /// Metoda tworzaca polaczenie miedzy klientam a serwisem
        /// </summary
        private void Consumer_Received(object sender, BasicDeliverEventArgs e)
        {
            //Powiadomienie, że zaczęto operacje na wiadomości
            semaphore2.WaitOne(-1, true);

            //Odbieranie wiadomości
            var body = e.Body.ToArray();
            var message = Encoding.UTF8.GetString(body);

            
            //Zmiana stanu zadania i dopisanie aktulnego zadania
            stanZadania = StanZadania.Pracuje;
            zadanie = message;

            //Wysłanie wiadomości o pobraniu zadania
            DodajWpis(message, StanZadania.Pobrano);


            try
            {
                //Wpisanie informacji do logu
                {
                    string txtLog = "[" + DateTime.Now.ToString() + "] Pobranie wiadomości";
                    WriteInFile(_nazwa.ToString(), txtLog);
                }
                
                List<string> variables = new List<string>();
                string path = message.Substring(message.IndexOf("\'"), message.LastIndexOf("\'") + 1);
                string arguments = message.Substring(path.Length+1);
                path = "\"" +path.Substring(1, path.Length - 2)+"\\"+arguments.Substring(arguments.IndexOf("ID") + 2, arguments.IndexOf(" ") - 2) + "\"";
                foreach (string word in arguments.Split(" ", StringSplitOptions.RemoveEmptyEntries))
                {
                    variables.Add(word);
                }

                arguments = "";
                for (int i = 2; i < variables.Count; i++)
                {
                    arguments += variables[i] + " ";
                }
                //if (!Directory.Exists(path)) Directory.CreateDirectory(path.Substring(1,path.Length-2));
                //Uruchomienie Procesu obliczeniowego
                ProcessStartInfo testowy = new ProcessStartInfo(variables[1]);
                //testowy.WorkingDirectory= path.Substring(1, path.Length-2);
                testowy.Arguments = arguments;
                var proc = Process.Start(testowy);
            
                //Wysłanie wiadomości o rozpoczęciu obliczeń
                DodajWpis(message, StanZadania.Rozpoczeto);

                //Wpisanie informacji do logu
                {
                    string txtLog = "[" + DateTime.Now.ToString() + "] Uruchomienie Programu Argumenty(" + arguments + ")";
                    WriteInFile(_nazwa.ToString(), txtLog);
                }
                //Oczekiwanie na zakończenie procesu
                proc.WaitForExit();
            
                 //Wpisanie informacji do logu
                {
                    string txtLog = "[" + DateTime.Now.ToString() + "] Koniec Obliczeń";
                    WriteInFile(_nazwa.ToString(), txtLog);
                }
            
                //Sprawdzenie kodu wyjściowego programu i wysłanie wiadomości o zakończeniu obliczeń
                if(proc.ExitCode==0) DodajWpis(message, StanZadania.ZakonczonoPowodzeniem);
                else DodajWpis(message, StanZadania.ZakonczonoNiePowodzeniem , "/"+proc.ExitCode.ToString());
                
                //Potwierdzenie wykonania operacji do kolejki
                ((EventingBasicConsumer)sender).Model.BasicAck(e.DeliveryTag, false);
            }
            catch (ArgumentOutOfRangeException ex)
            {
                MessageBox.Show("Bledna sciezka");
                stanZadania = StanZadania.Blad;
                ((EventingBasicConsumer)sender).Model.BasicAck(e.DeliveryTag, false);
                ForceStop();
            }
            catch (System.ComponentModel.Win32Exception ex)
            {
                MessageBox.Show("Błąd uruchamiania programu\n"+ex.Message);
                stanZadania = StanZadania.Blad;
                ((EventingBasicConsumer)sender).Model.BasicAck(e.DeliveryTag, false);
                ForceStop();
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message);
                stanZadania = StanZadania.Blad;
                ForceStop();
            }
            
            //Ustawienie stanu gotowości na kolejne zadanie
            if (!flagStop) stanZadania = StanZadania.Czeka;
            else stanZadania = StanZadania.Koniec;
            zadanie = "Brak zadania";
            message = null;
            semaphore.Set();
            semaphore2.Set();
        }


        /// <summary>
        /// Metoda łącząca się z kolejką i przekazująca informacje o statusie zadanai
        /// </summary
        private void DodajWpis(string msg, StanZadania stan, string blod="")
        {
            var factory = new ConnectionFactory();
            KonfigurujPolaczenie(ref factory);

            try
            {
                using (var connection = factory.CreateConnection())
                {
                    using (var channel = connection.CreateModel())
                    {
                        if (blod != "") stan = StanZadania.ZakonczonoNiePowodzeniem;
                        string message = msg + " " + Konfiguracja.getStan(stan) + blod;
                        var body = Encoding.UTF8.GetBytes(message);
                        channel.BasicPublish(
                            exchange: "",
                            routingKey: _config.RabitMQ_QueueSend,
                            basicProperties: null,
                            body: body
                            ); ;

                    }
                }
            }
            catch (RabbitMQ.Client.Exceptions.BrokerUnreachableException ex)
            {
                MessageBox.Show("Błąd połączenia\nHostname: " + factory.HostName + "\nUserName:" + factory.UserName + "\nPass:" + factory.Password + "\nVhost:" + _config.RabitMQ_VirualHost + "\n" + ex.Message);
            }catch (Exception e)
            {
                MessageBox.Show(e.Message);
            }
        }

        /// <summary>
        /// Metoda tworząca log
        /// </summary
        private void WriteInFile(string file, string txt)
        {
            file += ".txt";
            //if (!File.Exists(file)) File.Create(file);
            using (StreamWriter sw = File.AppendText(file))
            {
                sw.WriteLine(txt);
            }
        }

        /// <summary>
        /// Metoda wymuszenie zakończenia pracy wszystkich wątków
        /// </summary
        public void ForceStop()
        {
            flagStop = true;
            flagIsRuning = false;
            if (thread!=null)thread.Interrupt();
        }
    }
}
