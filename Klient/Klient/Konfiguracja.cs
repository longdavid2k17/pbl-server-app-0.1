using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;

namespace Klient
{
    public class Konfiguracja
    {
        //Sterowanie klientami
        /// <summary>
        /// Pole przechowujące maksymalną ilość aktywnych klientów
        /// </summary
        private int _iloscKlientow;

        /// <summary>
        /// Lista przechowująca nazwy klientów
        /// </summary
        private List<String> _klienciNazwy = new List<string>();

        //RabitMQ
        /// <summary>
        /// Pole przechowujące nazwę użytkownika RabitMQ
        /// </summary
        private string _rabitMQ_UserName;

        /// <summary>
        /// Pole przechowujące hasło użytkownika RabitMQ
        /// </summary
        private string _rabitMQ_Password;

        /// <summary>
        /// Pole przechowujące nazwę virtualHosta RabitMQ
        /// </summary
        private string _rabitMQ_VirualHost;


        /// <summary>
        /// Pole przechowujące nazwę hosta RabitMQ
        /// </summary
        private string _rabitMQ_HostName;

        /// <summary>
        /// Pole przechowujące port hosta RabitMQ
        /// </summary
        private int _rabitMQ_Port=0;

        /// <summary>
        /// Pole przechowujące ilosc kolejek z zadaniami RabitMQ
        /// </summary
        private int _rabitMQ_ilKolejek;

        /// <summary>
        /// Pole przechowujące nazwę kolejki z zadaniami RabitMQ
        /// </summary
        private List<string> _rabitMQ_QueueRecive= new List<string>();

        /// <summary>
        /// Pole przechowujące nazwę kolejki z rezultatami RabitMQ
        /// </summary
        private string _rabitMQ_QueueSend;

        //Ścieżka do programu
        /// <summary>
        /// Pole przechowujące ścieżkę do programu obliczeniowego
        /// </summary
        private string _programPath;

        //Inne
        /// <summary>
        /// Flaga poprawności odczytancyh danych
        /// </summary
        private bool _flagPoprawne;

        public int IloscKlientow { get => _iloscKlientow; set => _iloscKlientow = value; }
        public string RabitMQ_UserName { get => _rabitMQ_UserName; set => _rabitMQ_UserName = value; }
        public string RabitMQ_Password { get => _rabitMQ_Password; set => _rabitMQ_Password = value; }
        public string RabitMQ_VirualHost { get => _rabitMQ_VirualHost; set => _rabitMQ_VirualHost = value; }
        public string RabitMQ_HostName { get => _rabitMQ_HostName; set => _rabitMQ_HostName = value; }
        public string RabitMQ_QueueSend { get => _rabitMQ_QueueSend; set => _rabitMQ_QueueSend = value; }
        public string ProgramPath { get => _programPath; set => _programPath = value; }
        public bool FlagPoprawne { get => _flagPoprawne; set => _flagPoprawne = value; }
        public List<string> KlienciNazwy { get => _klienciNazwy; set => _klienciNazwy = value; }
        public int RabitMQ_ilKolejek { get => _rabitMQ_ilKolejek; set => _rabitMQ_ilKolejek = value; }
        public List<string> RabitMQ_QueueRecive { get => _rabitMQ_QueueRecive; set => _rabitMQ_QueueRecive = value; }
        public int RabitMQ_Port { get => _rabitMQ_Port; set => _rabitMQ_Port = value; }

        //Metody

        public void ZaladujDaneTestowe()
        {
            _iloscKlientow = 4;
            _rabitMQ_UserName= "yvflfnop";
            _rabitMQ_Password= "ckRS31S-iOuF057mBG5YJz1llA_t28vk";
            _rabitMQ_VirualHost= "yvflfnop";
            _rabitMQ_HostName = "dove.rmq.cloudamqp.com";
            RabitMQ_Port = 5672;
            _rabitMQ_ilKolejek = 1;
            _rabitMQ_QueueRecive.Add("tasks");
            _rabitMQ_QueueSend = "results";
            _programPath = (@"TestProgram.exe");
            //_programPath= (@"C:\Users\Daniel\source\repos\TestProgram\Debug\TestProgram.exe");
            _klienciNazwy.Add("Klient1");
            _klienciNazwy.Add("Klient2");
            _klienciNazwy.Add("Klient3");
            _klienciNazwy.Add("Klient4");
            FlagPoprawne = true;

        }

        public void ZaladujDaneTestowe2()
        {
            _iloscKlientow = 4;
            _rabitMQ_UserName = "guest";
            _rabitMQ_Password = "guest";
            _rabitMQ_VirualHost = "";
            _rabitMQ_HostName = @"localhost";
            RabitMQ_Port = 5672;
            _rabitMQ_ilKolejek = 2;
            _rabitMQ_QueueRecive.Add("tasks");
            _rabitMQ_QueueRecive.Add("tasks2");
            _rabitMQ_QueueSend = "results";
            _programPath = (@"TestProgram.exe");
            _klienciNazwy.Add("Klient1");
            _klienciNazwy.Add("Klient2");
            _klienciNazwy.Add("Klient3");
            _klienciNazwy.Add("Klient4");
            FlagPoprawne = true;

        }

        /// <summary>
        /// Metoda zapisująca konfiguracje do pliku
        /// </summary
        public void ZapiszKonfiguracje()
        {
            using (StreamWriter sw = new StreamWriter("config.cfg"))
            {
                sw.WriteLine("RabitMQ UserName= " + _rabitMQ_UserName);
                sw.WriteLine("RabitMQ Password= " + _rabitMQ_Password);
                sw.WriteLine("RabitMQ VHostName= " + _rabitMQ_VirualHost);
                sw.WriteLine("RabitMQ HostName= " + _rabitMQ_HostName);
                sw.WriteLine("RabitMQ Port= " + _rabitMQ_Port);
                sw.WriteLine("RabitMQ ilosc Kolejek= " + _rabitMQ_ilKolejek.ToString());
                for(int i =0; i< _rabitMQ_ilKolejek; i++)
                {
                    sw.WriteLine("Nazwa kolejki z zadaniami= " + _rabitMQ_QueueRecive.ElementAt(i));
                }
                //sw.WriteLine("Nazwa kolejki z zadaniami= " + RabitMQ_QueueRecive);
                sw.WriteLine("Nazwa kolejki z potwierdzeniami= " + _rabitMQ_QueueSend);
                sw.WriteLine("Ilosc klientow= " + _iloscKlientow.ToString());
                for(int i = 0; i < IloscKlientow; i++)
                {
                    sw.WriteLine("Klient "+i+"= " + KlienciNazwy.ElementAt(i));
                }
            }
        }

        /// <summary>
        /// Metoda odczytująca konfiguracje z pliku
        /// </summary
        public bool ZaladujDane()
        {
            using (StreamReader sr = new StreamReader("config.cfg"))
            {
                //Zmienne pomocnicze
                _flagPoprawne = false;
                string rabitMqUserName = null;
                string rabitMqPassword = null;
                string rabitMqVHostName = null;
                string rabitMqHostName = null;
                string rabitMqPort = null;
                string rabitMqQueueS = null;
                string programPath = null;
                string iloscKientow = null;
                string iloscKolejek = null;
                //Odczytywanie pliku
                if ((rabitMqUserName = sr.ReadLine()) == null) { _flagPoprawne = false; return false; }
                if ((rabitMqPassword = sr.ReadLine()) == null) { _flagPoprawne = false; return false; }
                if ((rabitMqVHostName = sr.ReadLine()) == null) { _flagPoprawne = false; return false; }
                if ((rabitMqHostName = sr.ReadLine()) == null) { _flagPoprawne = false; return false; }
                if ((rabitMqPort = sr.ReadLine()) == null) { _flagPoprawne = false; return false; }
                if ((iloscKolejek = sr.ReadLine()) == null) { _flagPoprawne = false; return false; }
                try
                {
                    RabitMQ_ilKolejek = int.Parse(iloscKolejek.Substring(iloscKolejek.IndexOf("=") + 2));
                }catch(Exception ex)
                {
                    _flagPoprawne = false;
                    return false;
                }
                for(int i=0; i < RabitMQ_ilKolejek; i++)
                {
                    string s = sr.ReadLine();
                    RabitMQ_QueueRecive.Add(s.Substring(s.IndexOf("=") + 2));
                }
                //if ((rabitMqQueueR = sr.ReadLine()) == null) { _flagPoprawne = false; return false; }
                if ((rabitMqQueueS = sr.ReadLine()) == null) { _flagPoprawne = false; return false; }
                if ((iloscKientow = sr.ReadLine()) == null) { _flagPoprawne = false; return false; }
                
                //Zmiana wartosci odczytanych na użyteczne dane
                try
                {
                    _rabitMQ_UserName = rabitMqUserName.Substring(rabitMqUserName.IndexOf("=") + 2);
                    _rabitMQ_Password = rabitMqPassword.Substring(rabitMqPassword.IndexOf("=") + 2);
                    _rabitMQ_VirualHost = rabitMqVHostName.Substring(rabitMqVHostName.IndexOf("=") + 2);
                    _rabitMQ_HostName = rabitMqHostName.Substring(rabitMqHostName.IndexOf("=") + 2);
                    _rabitMQ_Port = int.Parse(rabitMqPort.Substring(rabitMqPort.IndexOf("=") + 2));
                    _rabitMQ_QueueSend = rabitMqQueueS.Substring(rabitMqQueueS.IndexOf("=") + 2);
                    _iloscKlientow = int.Parse(iloscKientow.Substring(iloscKientow.IndexOf("=") + 2));
                    for(int i=0;i<_iloscKlientow;i++){
                        string s = sr.ReadLine();
                        _klienciNazwy.Add(s.Substring(s.IndexOf("=") + 2));
                    }
                }
                catch (Exception ex)
                {
                    _flagPoprawne = false;
                    return false;
                }

                //Ustaienie flag na powodzenie
                _flagPoprawne = true;
                return true;
            }
        }

        /// <summary>
        /// Metoda zwracająca łańcuch znakowy odopiwadający danemu stanowi
        /// </summary
        static public string getStan(StanZadania stanZadania)
        {
            switch (stanZadania)
            {
                case StanZadania.NieUruchomione:
                    return "Klient nie został uruchomiony";
                case StanZadania.Czeka:
                    return "Oczekuje na zadanie";
                case StanZadania.Pracuje:
                    return "Pracuje";
                case StanZadania.KończyZadanie:
                    return "Kończenie zadania";
                case StanZadania.Koniec:
                    return "Koniec zadania";
                case StanZadania.ZakonczonoPowodzeniem:
                    return "zakonczono";
                case StanZadania.ZakonczonoNiePowodzeniem:
                    return "niepowodzenie";
                case StanZadania.Pobrano:
                    return "pobrano";
                case StanZadania.Rozpoczeto:
                    return "rozpoczeto";
                case StanZadania.Blad:
                    return "blad";
            }
            return "Status Nieznany";
        }

        /// <summary>
        /// Metoda dodająca nazwę klienta
        /// </summary
        public void dodajNazw(String s)
        {
            _klienciNazwy.Add(s);
        }
    }

    public enum StanZadania
    {
        NieUruchomione,
        Czeka,
        Pracuje,
        KończyZadanie,
        Koniec,
        ZakonczonoPowodzeniem,
        ZakonczonoNiePowodzeniem,
        Pobrano,
        Rozpoczeto,
        Blad

    };
}
