using System;
using System.Collections.Generic;
using System.IO;
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
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        /// <summary>
        /// Lista przechowująca aktywnych klientów
        /// </summary>
        private List<KlientRabbit> klienci = new List<KlientRabbit>();

        /// <summary>
        /// Pole przechowujące konfiguracje
        /// </summary>
        Konfiguracja conf = new Konfiguracja();

        //Pola przechowujące kontrolki

        /// <summary>
        /// Pole przechowujące kontrolkę widoku głównego
        /// </summary>
        OknoGlowne oknoGlowne =null;

        /// <summary>
        /// Pole przechowujące kontrolkę odpowiedzialną za zminę konfiguracji
        /// </summary>
        KonfiguracjaForm konfiguracjaForm =null;

        /// <summary>
        /// Pole przechowujące kontrolkę z pełnym podglądem klienta
        /// </summary>
        KlientOkno klientOkno =null;

        public MainWindow()
        {
            //Inicjalizacja okna
            InitializeComponent();

            //Ładowanie konfiguracji
            //conf.ZaladujDaneTestowe();
            ZaladujKonfiguracje();
            //conf.ZaladujDaneTestowe2();
            //Tworzenie instancji kontrolek

            oknoGlowne = new OknoGlowne(conf,this);
            konfiguracjaForm = new KonfiguracjaForm(conf);

            //Dodawnie widoku głównego do pola grid
            grid_okno.Children.Add(oknoGlowne);
        }



        /// <summary>
        /// Metoda odpowiedzialna za załadowanie konfiguracji domyślnej lub z pliku
        /// </summary>
        private void ZaladujKonfiguracje()
        {
            string file = "config.cfg";
            if (File.Exists(file))
            {
                if (!conf.ZaladujDane())
                {
                    MessageBox.Show("Błąd w ładowaniu konfiguracji\nŁadowanie domyślnej");
                    conf.ZaladujDaneTestowe2();
                }
            }
            else
            {
                MessageBox.Show("Nie znaleziono pliku konfiguracyjnego\n Ładowanie domyślnej konfiguracji");
                conf.ZaladujDaneTestowe2();
                conf.ZapiszKonfiguracje();
            }
        }

        //Metody

        /// <summary>
        /// Zmiana wyswietlanego elementu na okno konfiguracji
        /// </summary>
        public void PrzejdzDoKonfiguracji()
        {
            if (klienci.Count > 0) konfiguracjaForm.ModyfikacjaKlientow = false;
            konfiguracjaForm.Aktualizuj();
            grid_okno.Children.Clear();
            grid_okno.Children.Add(konfiguracjaForm);
        }

        /// <summary>
        /// Zwraca instancje klienta o zadanym id
        /// </summary>
        public KlientRabbit PobierzKlienta(int index)
        {
            return klienci.ElementAt(index);
        }

        /// <summary>
        /// Dodaje klienta do listy aktywnych klientów, oraz tworzy przycisk do jego okna
        /// </summary>
        public void DodajKlienta(KlientRabbit klient)
        {
            //Dodanie klienta do listy
            klienci.Add(klient);

            //Tworzenie nowego przycisku
            Button btn = new Button();
            btn.Content = klient.getNazwa();
            btn.Margin = new Thickness(0, 10, 0, 1);
            btn.Height = 35;
            btn.Tag = (klienci.Count() - 1);
            btn.Click += btn_klientClikc;
            btn.Foreground = Brushes.White;
            btn.Background = Brushes.Transparent;
            btn.Name = "btn_klient" + klienci.Count().ToString();

            //Dodawanie przycisku do pola menu
            grid_menu1.RowDefinitions.Add(new RowDefinition());
            grid_menu1.Height += 50;
            Grid.SetRow(btn, klienci.Count() - 1);
            grid_menu1.Children.Add(btn);
        }

        //Reakcja na zdarzenia

        /// <summary>
        /// Zmiana wyswietlanego elementu na widok główny
        /// </summary>
        private void btn_glowne_Click(object sender, RoutedEventArgs e)
        {
            grid_okno.Children.Clear();
            oknoGlowne.Aktualizuj(conf);
            grid_okno.Children.Add(oknoGlowne);
        }

        /// <summary>
        /// Zmiana wyswietlanego elementu na okno konfiguracji
        /// </summary>
        private void btn_konfiguracja_Click(object sender, RoutedEventArgs e)
        {
            PrzejdzDoKonfiguracji();
        }

        /// <summary>
        /// Zmiana wyswietlanego elementu na okno danego klienta
        /// </summary>
        private void btn_klientClikc(object sender, RoutedEventArgs e)
        {
            int index = int.Parse(((Button)sender).Tag.ToString());
            if (klientOkno != null)
            {
                klientOkno.ForceStop();
            }
            klientOkno = new KlientOkno(klienci.ElementAt(index));
            grid_okno.Children.Clear();
            grid_okno.Children.Add(klientOkno);
        }


        /// <summary>
        /// Wyłączanie wszystkich wątków działających w tle
        /// </summary>
        private void Window_Closing(object sender, System.ComponentModel.CancelEventArgs e)
        {
            if (oknoGlowne != null) oknoGlowne.ForeStop();
            if (klientOkno != null) klientOkno.ForceStop();
        }
    }
}
