using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace Klient
{
    /// <summary>
    /// Logika interakcji dla klasy KonfiguracjaForm.xaml
    /// </summary>
    public partial class KonfiguracjaForm : UserControl
    {
        /// <summary>
        /// Pole przechowujące konfiguracje
        /// </summary
        private Konfiguracja _conf;

        /// <summary>
        /// Pole przechowujące informacje o umożliwieniu modyfikacji klientów
        /// </summary
        bool modyfikacjaKlientow=true;

        /// <summary>
        /// Pole przechowujące informacje o rzowinięciu opcji konfiguracyjnych rabbita
        /// </summary
        bool rabbit = false;

        /// <summary>
        /// Pole przechowujące informacje o rzowinięciu opcji konfiguracyjnych klientów
        /// </summary
        bool klienci = false;

        bool kolejki = false;

        /// <summary>
        /// Lista przechowująca pola tekstowe do konfiguracji nazw klientów
        /// </summary
        List<TextBox> textBoxes = new List<TextBox>();

        /// <summary>
        /// Lista przechowująca pola tekstowe do kolejek
        /// </summary
        List<TextBox> textBoxesQ = new List<TextBox>();

        public bool ModyfikacjaKlientow { get => modyfikacjaKlientow; set => modyfikacjaKlientow = value; }

        public KonfiguracjaForm(Konfiguracja konfiguracja)
        {
            //Inicjalizacja
            InitializeComponent();
            
            _conf = konfiguracja;

            //Wpisanie do pól tekstowych akyualnych wartości
            txt_klient.Text = _conf.IloscKlientow.ToString();
            txt_rabbit_user.Text = _conf.RabitMQ_UserName;
            txt_rabbit_pass.Text = _conf.RabitMQ_Password;
            txt_rabbit_vhost.Text = _conf.RabitMQ_VirualHost;
            txt_rabbit_host.Text = _conf.RabitMQ_HostName;
            txt_rabbit_port.Text = _conf.RabitMQ_Port.ToString();
            txt_ilKolejek.Text = _conf.RabitMQ_ilKolejek.ToString();
            txt_rabbitQueueS.Text = _conf.RabitMQ_QueueSend;
        }

        /// <summary>
        /// Metoda rozwijająca konfiguracje rabbita
        /// </summary
        private void btn_rabbit_Click(object sender, RoutedEventArgs e)
        {
            if (!rabbit)
            {
                scr_rabbit.Height = 110;
                rabbit = !rabbit;
            }
            else
            {
                scr_rabbit.Height = 0;
                rabbit = !rabbit;
            }
        }

        /// <summary>
        /// Metoda rozwijająca konfiguracje klientów
        /// </summary
        private void btn_klienci_Click(object sender, RoutedEventArgs e)
        {
            if (!klienci)
            {
                List<String> s = _conf.KlienciNazwy;
                scr_klient.Height = 60;
                klienci = !klienci;

                for (int i = 0; i < _conf.IloscKlientow; i++)
                {
                    grid_klienci.RowDefinitions.Add(new RowDefinition());
                    Label lbl = new Label();
                    lbl.Content = "Klient NR:" +i.ToString();
                    lbl.Margin = new Thickness(10, 0, 0, 0);
                    lbl.Foreground = Brushes.White;
                    lbl.Height = 30;
                    Grid.SetRow(lbl, i*2);
                    grid_klienci.Children.Add(lbl);

                    grid_klienci.RowDefinitions.Add(new RowDefinition());
                    TextBox textBox = new TextBox();
                    textBox.Margin= new Thickness(10, 0, 0, 0);
                    textBox.Width = 486;
                    textBox.Height = 30;
                    textBox.VerticalAlignment = VerticalAlignment.Top;
                    textBox.HorizontalAlignment = HorizontalAlignment.Left;
                    if (!modyfikacjaKlientow) textBox.IsEnabled = false;
                    if (s.Count > i) textBox.Text = s.ElementAt(i);
                    else textBox.Text = "Klient NR" + i;
                    textBox.Name = "txt_klient" + i;
                    textBoxes.Add(textBox);
                    Grid.SetRow(textBox, (i*2)+1);
                    grid_klienci.Children.Add(textBoxes.ElementAt(i));
                }
            }
            else
            {
                scr_klient.Height = 0;
                klienci = !klienci;
                textBoxes.Clear();
            }
            
        }

        /// <summary>
        /// Metoda zapisująca wprowadzone dane tymczasowo
        /// </summary
        private void btn_zapis_Click(object sender, RoutedEventArgs e)
        {
            string klientNum, rabbitMQUser, rabbitMQOPass, rabbitMQVHost, rabbitMQHost, rabbitMQQueueS, rabitMQPort, iloscKolejek;
            int ilKlientow,ilKolejek;
            klientNum=txt_klient.Text;
            rabbitMQUser=txt_rabbit_user.Text;
            rabbitMQOPass=txt_rabbit_pass.Text;
            rabbitMQVHost=txt_rabbit_vhost.Text;
            rabbitMQHost=txt_rabbit_host.Text;
            iloscKolejek =txt_ilKolejek.Text;
            rabbitMQQueueS=txt_rabbitQueueS.Text;
            rabitMQPort = txt_rabbit_port.Text;
            try
            {
                ilKlientow = int.Parse(klientNum);
                if (klienci)
                {
                    List<String> s = new List<string>();
                    for (int i = 0; i < _conf.IloscKlientow; i++)
                    {
                        s.Add(textBoxes.ElementAt(i).Text);
                    }
                    if (_conf.IloscKlientow < ilKlientow)
                    {
                        for (int i = _conf.IloscKlientow; i < ilKlientow; i++)
                        {
                            s.Add("Klient " + i);
                        }
                    }
                    _conf.KlienciNazwy = s;
                }
                else
                {
                    for (int i = _conf.IloscKlientow; i < ilKlientow; i++)
                    {
                        _conf.dodajNazw("Klient " + i);
                    }
                }
                ilKolejek = int.Parse(iloscKolejek);
                List<string> queues = new List<string>();
                if(kolejki){ 
                for (int i = 0; i < _conf.RabitMQ_ilKolejek; i++)
                {
                    queues.Add(textBoxesQ.ElementAt(i).Text);
                }
                if (_conf.RabitMQ_ilKolejek < ilKolejek)
                {
                    for (int i = _conf.RabitMQ_ilKolejek; i < ilKolejek; i++)
                    {
                        queues.Add("queue " + i);
                    }
                }
                    _conf.RabitMQ_QueueRecive = queues;
                }



                _conf.IloscKlientow = ilKlientow;
                _conf.RabitMQ_UserName = rabbitMQUser;
                _conf.RabitMQ_Password = rabbitMQOPass;
                _conf.RabitMQ_VirualHost = rabbitMQVHost;
                _conf.RabitMQ_HostName = rabbitMQHost;
                _conf.RabitMQ_ilKolejek = ilKolejek;
                _conf.RabitMQ_QueueSend = rabbitMQQueueS;
                _conf.RabitMQ_Port = int.Parse(rabitMQPort);
            }catch(Exception ex)
            {
                MessageBox.Show("Zła liczba klientow");
            }

        }

        /// <summary>
        /// Metoda zapisująca wprowadzone dane na stałe
        /// </summary
        private void btn_zapisDoPliku_Click(object sender, RoutedEventArgs e)
        {
            this.btn_zapis_Click(sender,e);
            _conf.ZapiszKonfiguracje();
        }

        /// <summary>
        /// Metoda aktualizująca dane do konfiguracji
        /// </summary
        public void Aktualizuj()
        {
            if (!modyfikacjaKlientow) txt_klient.IsEnabled = false;
        }

        private void btn_kolejka_Click(object sender, RoutedEventArgs e)
        {
            if (!kolejki)
            {
                scr_kolejka.Height = 60;
                kolejki = !kolejki;
                List<String> s = _conf.RabitMQ_QueueRecive;
                for (int i = 0; i < _conf.RabitMQ_ilKolejek; i++)
                {
                    grid_kolejka.RowDefinitions.Add(new RowDefinition());
                    Label lbl = new Label();
                    lbl.Content = "Kolejka NR:" + i.ToString();
                    lbl.Margin = new Thickness(10, 0, 0, 0);
                    lbl.Foreground = Brushes.White;
                    lbl.Height = 30;
                    Grid.SetRow(lbl, i * 2);
                    grid_kolejka.Children.Add(lbl);

                    grid_kolejka.RowDefinitions.Add(new RowDefinition());
                    TextBox textBox = new TextBox();
                    textBox.Margin = new Thickness(10, 0, 0, 0);
                    textBox.Width = 486;
                    textBox.Height = 30;
                    textBox.VerticalAlignment = VerticalAlignment.Top;
                    textBox.HorizontalAlignment = HorizontalAlignment.Left;
                    if (!modyfikacjaKlientow) textBox.IsEnabled = false;
                    if (s.Count > i) textBox.Text = s.ElementAt(i);
                    else textBox.Text = "Kolejka NR" + i;
                    textBox.Name = "txt_Kolejka" + i;
                    textBoxesQ.Add(textBox);
                    Grid.SetRow(textBox, (i * 2) + 1);
                    grid_kolejka.Children.Add(textBoxesQ.ElementAt(i));
                }
            }
            else
            {
                scr_kolejka.Height = 0;
                kolejki = !kolejki;
                textBoxesQ.Clear();
            }
        }
    }
}
