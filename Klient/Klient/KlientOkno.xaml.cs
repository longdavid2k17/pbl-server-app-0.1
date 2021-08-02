using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading;
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
    /// Logika interakcji dla klasy KlientOkno.xaml
    /// </summary>
    public partial class KlientOkno : UserControl
    {
        /// <summary>
        /// Pole przechowujące klienta
        /// </summary>
        KlientRabbit _klient;

        /// <summary>
        /// Pole przechowujące informacje o dostępności przycisku start
        /// </summary>
        private bool buttonStart = true;

        /// <summary>
        /// Pole przechowujące informacje o dostępności przycisku stop
        /// </summary>
        private bool buttonStop = false;

        /// <summary>
        /// Pole przechowujące informacje o zakonczeniu pracy wątka w tle
        /// </summary>
        private bool backgroudWorkEnd = true;

        /// <summary>
        /// Pole przechowujące informacje o zakończeniu pracy
        /// </summary>
        private bool flagStop = false;

        /// <summary>
        /// Pole przechowujące wątek działający w tle
        /// </summary>
        private Thread watekAktualizacjaStatusu = null;

        public KlientOkno(KlientRabbit klient)
        {
            //Inicjalizacja
            InitializeComponent();

            //Wpisanie argumentów do pól
            this._klient = klient;

            //Wypisanie infromacji w etykietach
            lbl_nazwa.Content = _klient.getNazwa();
            
            KonfiguracjaFlag();
            KonfiguracjaPrzyciskow();
            
            //Uruchomienie automatycznej aktualizacji stausu
            watekAktualizacjaStatusu = new Thread(() => aktualizujStatus(2000));
            watekAktualizacjaStatusu.Start();
            backgroudWorkEnd = false;
        }

        //Metody

        /// <summary>
        /// Metoda umożliwiająca zmiane stanów przycisków asynchronicznie
        /// </summary>
        private void KonfiguracjaPrzyciskow()
        {
            bool uiAccessBtnStart = btn_start.Dispatcher.CheckAccess();
            bool uiAccessBtnStop = btn_stop.Dispatcher.CheckAccess();
            if (uiAccessBtnStart) btn_start.IsEnabled = buttonStart;
            else btn_start.Dispatcher.Invoke(() => btn_start.IsEnabled = buttonStart);
            if (uiAccessBtnStop) btn_stop.IsEnabled = buttonStop;
            else btn_stop.Dispatcher.Invoke(() => btn_stop.IsEnabled = buttonStop);
        }

        /// <summary>
        /// Metoda pobierająca dane o stanie klienta i przypisująca wartości do pól zarządzających przyciskami
        /// </summary
        private void KonfiguracjaFlag()
        {
            buttonStart = !_klient.getStart();
            buttonStop = !buttonStart;
        }

        /// <summary>
        /// Metoda wymuszająca zkończenie pracy watków
        /// </summary
        public void ForceStop()
        {
            flagStop = true;
            _klient.ForceStop();
            if (watekAktualizacjaStatusu != null) watekAktualizacjaStatusu.Interrupt();
        }

        /// <summary>
        /// Metoda wypisujaca informacje asynchronicznie
        /// </summary
        private void aktualizujStatus(int time)
        {
            try
            {
                while (!flagStop)
                {
                    bool uiAccessStatus = lbl_Status.Dispatcher.CheckAccess();
                    bool uiAccessZadanie = lbl_Zadanie.Dispatcher.CheckAccess();
                    if (uiAccessStatus) lbl_Status.Content = Konfiguracja.getStan(_klient.getStan());
                    else lbl_Status.Dispatcher.Invoke(() => { lbl_Status.Content = Konfiguracja.getStan(_klient.getStan()); });
                    if (uiAccessZadanie) lbl_Zadanie.Text = _klient.getZadanie();
                    else lbl_Zadanie.Dispatcher.Invoke(() => { lbl_Zadanie.Text = _klient.getZadanie(); });
                        Thread.Sleep(time);
                        if (_klient.getStan() == StanZadania.Koniec)
                        {
                            buttonStart = true;
                            buttonStop = false;
                            flagStop = true;
                            KonfiguracjaPrzyciskow();
                            if (uiAccessStatus) lbl_Status.Content = Konfiguracja.getStan(_klient.getStan());
                            else lbl_Status.Dispatcher.Invoke(() => { lbl_Status.Content = Konfiguracja.getStan(_klient.getStan()); });
                        }
                }
            }
            catch (Exception ex)
            {
                Debug.WriteLine("Przerwanie watku klientOkno");
            }
            backgroudWorkEnd = true;
        }

        //rekcje na zdarzenia

        /// <summary>
        /// Uruchom klienta
        /// </summary
        private void btn_start_Click(object sender, RoutedEventArgs e)
        {
            _klient.KlientStart();
            buttonStart = false;
            buttonStop = true;
            flagStop = false;
            KonfiguracjaPrzyciskow();
            if (backgroudWorkEnd)
            {
                watekAktualizacjaStatusu = new Thread(() => aktualizujStatus(2000));
                watekAktualizacjaStatusu.Start();
            }
        }

        /// <summary>
        /// Otwórz Log
        /// </summary
        private void btn_log_Click(object sender, RoutedEventArgs e)
        {
            Process.Start("notepad.exe", (_klient.getNazwa() + ".txt"));
        }

        /// <summary>
        /// Zatrzymaj klienta
        /// </summary
        private void btn_stop_Click(object sender, RoutedEventArgs e)
        {
            _klient.KlientStop();
        }
    }
}
