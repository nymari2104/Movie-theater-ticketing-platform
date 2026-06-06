import { useState, useEffect } from 'react';
import { 
  FilmReel, 
  Calendar, 
  Clock, 
  ArrowLeft, 
  Warning, 
  Ticket, 
  Download, 
  Sparkle, 
  X, 
  ArrowRight,
  Database,
  ShieldCheck,
  Seat as SeatIcon
} from '@phosphor-icons/react';
import { motion, AnimatePresence } from 'motion/react';
import { api } from './services/api';
import type { Movie, Showtime, Seat, Booking } from './services/api';

export default function App() {
  // Application states
  const [movies, setMovies] = useState<Movie[]>([]);
  const [selectedMovie, setSelectedMovie] = useState<Movie | null>(null);
  const [showtimes, setShowtimes] = useState<Showtime[]>([]);
  const [selectedShowtime, setSelectedShowtime] = useState<Showtime | null>(null);
  const [selectedDate, setSelectedDate] = useState<string>("2026-06-05");
  const [seats, setSeats] = useState<Seat[]>([]);
  const [selectedSeats, setSelectedSeats] = useState<Seat[]>([]);
  const [seatsLoading, setSeatsLoading] = useState(false);
  const [isBooking, setIsBooking] = useState(false);
  const [bookingResult, setBookingResult] = useState<Booking | null>(null);
  const [bookingError, setBookingError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'home' | 'tickets'>('home');
  const [myTickets, setMyTickets] = useState<Booking[]>([]);
  const [isMockMode, setIsMockMode] = useState(api.isMockMode());

  // Spotlight index for Hero Carousel
  const [spotlightIndex, setSpotlightIndex] = useState(0);

  // Load movies on init and when mock mode toggles
  useEffect(() => {
    const fetchMovies = async () => {
      setLoading(true);
      try {
        const data = await api.getMovies();
        setMovies(data);
        // Sync the mock state back in case it was auto-enabled by empty list detection
        setIsMockMode(api.isMockMode());
      } catch (err) {
        console.error("Failed to load movies:", err);
      } finally {
        setLoading(false);
      }
    };
    fetchMovies();
  }, [isMockMode]);

  // Auto-rotate spotlight movie every 8 seconds
  useEffect(() => {
    if (movies.length === 0 || selectedMovie) return;
    const interval = setInterval(() => {
      setSpotlightIndex((prev) => (prev + 1) % movies.length);
    }, 8000);
    return () => clearInterval(interval);
  }, [movies, selectedMovie]);

  // Load showtimes when selected movie or date changes
  useEffect(() => {
    if (!selectedMovie) return;
    const fetchShowtimes = async () => {
      try {
        const data = await api.getShowtimes(selectedMovie.id, selectedDate);
        setShowtimes(data);
        setSelectedShowtime(null);
        setSelectedSeats([]);
      } catch (err) {
        console.error("Failed to load showtimes:", err);
      }
    };
    fetchShowtimes();
  }, [selectedMovie, selectedDate]);

  // Load seats when showtime changes
  useEffect(() => {
    if (!selectedShowtime) return;
    const fetchSeats = async () => {
      setSeatsLoading(true);
      setSeats([]);
      setSelectedSeats([]);
      try {
        const data = await api.getSeats(selectedShowtime.id);
        setSeats(data);
      } catch (err) {
        console.error("Failed to load seats:", err);
      } finally {
        setSeatsLoading(false);
      }
    };
    fetchSeats();
  }, [selectedShowtime]);

  // Handle seat click
  const handleSeatClick = (seat: Seat) => {
    if (seat.status === 'RESERVED') return;
    
    setSelectedSeats(prev => {
      const isAlreadySelected = prev.some(s => s.id === seat.id);
      if (isAlreadySelected) {
        return prev.filter(s => s.id !== seat.id);
      } else {
        // Max 6 seats per transaction
        if (prev.length >= 6) {
          alert("Tối đa đặt 6 ghế cho một giao dịch!");
          return prev;
        }
        return [...prev, seat];
      }
    });
  };

  // Submit booking (Saga flow)
  const handleConfirmBooking = async () => {
    if (!selectedShowtime || selectedSeats.length === 0) return;
    
    setIsBooking(true);
    setBookingError(null);
    setBookingResult(null);

    const seatIds = selectedSeats.map(s => s.id);
    const seatNumbers = selectedSeats.map(s => s.seatNumber);
    const price = selectedShowtime.ticketPrice || selectedShowtime.price || 90000;
    const totalPrice = price * selectedSeats.length;

    try {
      // Step 1: Send request, returns immediately with PENDING
      const booking = await api.createBooking({
        userId: "customer_novacine",
        showtimeId: selectedShowtime.id,
        seatIds,
        seatNumbers,
        bookingDate: selectedShowtime.showDate,
        totalPrice
      });

      setBookingResult(booking);

      // Step 2: Polling loop to wait for Kafka Saga outcome (CONFIRMED or CANCELLED)
      let attempts = 0;
      const maxAttempts = 15; // 15 attempts * 1.5s = ~22 seconds max
      
      const poll = setInterval(async () => {
        attempts++;
        try {
          const updatedBooking = await api.getBookingDetails(booking.id);
          console.log(`Polling attempt ${attempts}: status is ${updatedBooking.status}`);
          
          if (updatedBooking.status !== 'PENDING') {
            clearInterval(poll);
            setBookingResult(updatedBooking);
            setIsBooking(false);
            
            if (updatedBooking.status === 'CONFIRMED') {
              // Add to local myTickets memory
              setMyTickets(prev => [updatedBooking, ...prev]);
            } else {
              setBookingError("Giao dịch thanh toán bị từ chối bởi ngân hàng mô phỏng. Ghế của bạn đã được giải phóng.");
            }
          }
        } catch (err) {
          console.error("Error during booking status polling:", err);
        }

        if (attempts >= maxAttempts) {
          clearInterval(poll);
          setIsBooking(false);
          setBookingError("Hết thời gian chờ phản hồi giao dịch (Timeout). Vui lòng thử lại.");
        }
      }, 1500);

    } catch (err: any) {
      console.error("Failed to initiate booking:", err);
      setIsBooking(false);
      const errMsg = err.response?.data?.message || "Không thể thực hiện yêu cầu đặt vé. Có thể ghế đã được giữ bởi người khác.";
      setBookingError(errMsg);
    }
  };

  const handleResetAfterBooking = () => {
    setBookingResult(null);
    setBookingError(null);
    setSelectedSeats([]);
    // Reload seats to reflect changes
    if (selectedShowtime) {
      api.getSeats(selectedShowtime.id).then(setSeats);
    }
  };

  // Quick date choices for booking timeline
  const dates = [
    { label: "Hôm nay", dateStr: "2026-06-05", short: "05/06" },
    { label: "Ngày mai", dateStr: "2026-06-06", short: "06/06" },
    { label: "Chủ nhật", dateStr: "2026-06-07", short: "07/06" }
  ];

  // Current featured spotlight movie
  const spotlightMovie = movies[spotlightIndex];

  return (
    <div className="min-h-screen bg-brand-bg text-brand-text flex flex-col relative theater-gradient selection:bg-brand-accent selection:text-brand-bg">
      {/* Header */}
      <header className="border-b border-brand-border/60 bg-brand-bg/85 backdrop-blur-md sticky top-0 z-50">
        <div className="max-w-[1400px] mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-8">
            <a href="/" className="flex items-center gap-2" onClick={(e) => { e.preventDefault(); setSelectedMovie(null); handleResetAfterBooking(); setActiveTab('home'); }}>
              <span className="text-2xl font-bold tracking-tighter text-brand-accent flex items-center gap-1.5">
                NOVA<span className="text-white font-light">CINE</span>
                <span className="w-1.5 h-1.5 bg-brand-accent rounded-full animate-pulse"></span>
              </span>
            </a>
            <nav className="hidden md:flex items-center gap-6 text-sm font-medium">
              <a href="#" className={`transition-colors hover:text-brand-accent ${activeTab === 'home' && !selectedMovie ? 'text-brand-accent' : 'text-brand-muted'}`} 
                 onClick={(e) => { e.preventDefault(); setSelectedMovie(null); handleResetAfterBooking(); setActiveTab('home'); }}>Phim Đang Chiếu</a>
              <a href="#" className={`transition-colors hover:text-brand-accent ${activeTab === 'tickets' ? 'text-brand-accent' : 'text-brand-muted'}`} 
                 onClick={(e) => { e.preventDefault(); setActiveTab('tickets'); }}>Vé Của Tôi ({myTickets.length})</a>
            </nav>
          </div>
          
          <div className="flex items-center gap-3">
            {/* API Mode Indicator */}
            <button 
              onClick={() => {
                const nextMode = !isMockMode;
                api.enableMock(nextMode);
                setIsMockMode(nextMode);
              }}
              className="flex items-center gap-2 px-3 py-1.5 bg-brand-card border border-brand-border hover:border-brand-muted/40 rounded-full text-[11px] font-mono text-brand-muted cursor-pointer transition-all hover:scale-105 active:scale-95"
              title="Nhấp để chuyển đổi giữa kết nối API thật và Chế độ giả lập Demo"
            >
              {isMockMode ? (
                <>
                  <div className="w-1.5 h-1.5 rounded-full bg-amber-500 animate-pulse"></div>
                  <span>Saga Mock Active</span>
                </>
              ) : (
                <>
                  <div className="w-1.5 h-1.5 rounded-full bg-emerald-500"></div>
                  <span>API Connected</span>
                </>
              )}
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1 max-w-[1400px] mx-auto w-full px-6 py-8 relative">
        {activeTab === 'tickets' ? (
          /* My Tickets Tab */
          <div className="max-w-2xl mx-auto py-8">
            <h1 className="text-3xl font-bold tracking-tight mb-8 text-gradient">Lịch sử đặt vé của bạn</h1>
            {myTickets.length === 0 ? (
              <div className="bg-brand-card border border-brand-border/60 rounded-2xl p-12 text-center cinematic-glow">
                <Ticket className="w-12 h-12 text-brand-accent/50 mx-auto mb-4" />
                <h3 className="text-lg font-semibold mb-2">Chưa có vé nào được đặt</h3>
                <p className="text-sm text-brand-muted mb-6 max-w-sm mx-auto">Các vé xem phim bạn đặt thành công qua Saga Pattern sẽ xuất hiện tại đây.</p>
                <button 
                  onClick={() => setActiveTab('home')}
                  className="px-5 py-2.5 bg-brand-accent hover:bg-brand-accent-hover text-brand-bg font-semibold rounded-lg text-sm transition-all duration-200"
                >
                  Khám phá phim ngay
                </button>
              </div>
            ) : (
              <div className="space-y-6">
                {myTickets.map((ticket) => {
                  // Find the movie by showtimeId: look in all movies' showtimes or fallback
                  const movie = movies.find(m => m.id === ticket.showtimeId) ||
                    movies.find(m => m.id === selectedMovie?.id) ||
                    movies[0] ||
                    MOCK_MOVIES_FALLBACK[0];
                  return (
                    <RetroTicket key={ticket.id} booking={ticket} movie={movie} />
                  );
                })}
              </div>
            )}
          </div>
        ) : selectedMovie ? (
          /* Booking / Seat Selection Panel */
          <div className="py-2">
            {/* Breadcrumb / Back button */}
            <button 
              onClick={() => { setSelectedMovie(null); handleResetAfterBooking(); }}
              className="flex items-center gap-2 text-sm text-brand-muted hover:text-brand-accent transition-colors mb-6 group cursor-pointer"
            >
              <ArrowLeft className="w-4 h-4 transition-transform group-hover:-translate-x-1" />
              <span>Quay lại danh sách phim</span>
            </button>

            <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
              {/* Left Column: Movie Info Detail */}
              <div className="lg:col-span-4 bg-brand-card border border-brand-border/60 rounded-2xl p-6 relative overflow-hidden cinematic-glow">
                <div className="aspect-[2/3] w-full rounded-lg overflow-hidden mb-6 relative group">
                  <img 
                    src={selectedMovie.posterUrl || `https://picsum.photos/seed/${selectedMovie.title}/400/600`} 
                    alt={selectedMovie.title}
                    className="w-full h-full object-cover"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-brand-card via-transparent to-transparent opacity-80"></div>
                </div>

                <div className="flex items-center gap-2 mb-3">
                  <span className="px-2 py-0.5 bg-brand-accent/15 border border-brand-accent/30 text-brand-accent rounded text-xs font-mono font-bold">
                    {selectedMovie.rating || 'T13'}
                  </span>
                  <span className="text-xs text-brand-muted font-mono flex items-center gap-1">
                    <Clock className="w-3.5 h-3.5" /> {selectedMovie.duration} phút
                  </span>
                </div>

                <h2 className="text-2xl font-bold tracking-tight text-white mb-2">{selectedMovie.title}</h2>
                <p className="text-xs text-brand-accent font-mono mb-4">{selectedMovie.genre}</p>
                <p className="text-sm text-brand-muted leading-relaxed mb-6">{selectedMovie.description}</p>
                
                <div className="border-t border-brand-border/80 pt-4 flex flex-col gap-2.5 text-xs text-brand-muted font-mono">
                  <div className="flex justify-between">
                    <span>Khởi chiếu</span>
                    <span>{selectedMovie.releaseDate}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Hệ thống chiếu</span>
                    <span>NovaCine IMAX Cinema</span>
                  </div>
                </div>
              </div>

              {/* Right Column: Showtime & Seats Selector */}
              <div className="lg:col-span-8 space-y-6">
                {/* Showtime Selector Card */}
                <div className="bg-brand-card border border-brand-border/60 rounded-2xl p-6 relative">
                  <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
                    <Calendar className="w-5 h-5 text-brand-accent" />
                    <span>Chọn ngày chiếu & Suất chiếu</span>
                  </h3>
                  
                  {/* Calendar Timeline */}
                  <div className="flex gap-3 mb-6">
                    {dates.map((d) => (
                      <button
                        key={d.dateStr}
                        onClick={() => setSelectedDate(d.dateStr)}
                        className={`flex-1 py-3 px-4 rounded-xl border transition-all duration-200 flex flex-col items-center justify-center cursor-pointer ${
                          selectedDate === d.dateStr 
                            ? 'border-brand-accent bg-brand-accent/5 text-brand-accent shadow-[0_0_15px_rgba(229,169,59,0.05)]' 
                            : 'border-brand-border hover:border-brand-muted/40 text-brand-muted'
                        }`}
                      >
                        <span className="text-xs text-brand-muted font-mono mb-1">{d.label}</span>
                        <span className="text-sm font-bold font-mono">{d.short}</span>
                      </button>
                    ))}
                  </div>

                  {/* Showtimes List */}
                  <div>
                    <span className="text-xs text-brand-muted font-mono uppercase tracking-widest block mb-3">Suất chiếu khả dụng</span>
                    {showtimes.length === 0 ? (
                      <p className="text-sm text-brand-muted py-4">Không có suất chiếu nào vào ngày này. Vui lòng chọn ngày khác.</p>
                    ) : (
                      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                        {showtimes.map((st) => (
                          <button
                            key={st.id}
                            onClick={() => setSelectedShowtime(st)}
                            className={`py-3 px-4 rounded-xl border text-left transition-all duration-200 cursor-pointer ${
                              selectedShowtime?.id === st.id 
                                ? 'border-brand-accent bg-brand-accent/5 text-white' 
                                : 'border-brand-border hover:border-brand-muted/40 text-brand-muted'
                            }`}
                          >
                            <div className="text-base font-bold font-mono text-brand-accent mb-0.5">{st.startTime.substring(0, 5)}</div>
                            <div className="text-xs font-mono text-brand-muted flex justify-between">
                              <span>{st.roomName}</span>
                              <span>{(st.ticketPrice || st.price || 90000).toLocaleString()} đ</span>
                            </div>
                          </button>
                        ))}
                      </div>
                    )}
                  </div>
                </div>

                {/* IMAX Seats Selection Card */}
                {selectedShowtime && (
                  <motion.div 
                    initial={{ opacity: 0, y: 15 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.3 }}
                    className="bg-brand-card border border-brand-border/60 rounded-2xl p-6 relative overflow-hidden"
                  >
                    <h3 className="text-lg font-semibold mb-6 flex items-center gap-2">
                      <SeatIcon className="w-5 h-5 text-brand-accent" />
                      <span>Chọn vị trí ghế ({selectedSeats.length}/6)</span>
                    </h3>

                    {/* IMAX Screen Curve Illustration */}
                    <div className="w-full max-w-md mx-auto mb-12 text-center">
                      <div className="h-4 border-t-2 border-brand-accent/40 rounded-[50%/8px] relative opacity-85 cinematic-glow">
                        <span className="absolute -top-3 left-1/2 -translate-x-1/2 text-[10px] tracking-[0.25em] font-mono text-brand-accent font-semibold uppercase">
                          MÀN HÌNH IMAX SCREEN
                        </span>
                      </div>
                      <div className="text-[9px] text-brand-muted font-mono mt-1">GÓC NHÌN PHÒNG CHIẾU</div>
                    </div>

                    {/* Seat Grid */}
                    <div className="max-w-lg mx-auto overflow-x-auto pb-4">
                      {seatsLoading ? (
                        <div className="flex flex-col items-center justify-center py-12 gap-3">
                          <div className="w-8 h-8 border-2 border-brand-accent/30 border-t-brand-accent rounded-full animate-spin" />
                          <span className="text-xs text-brand-muted font-mono">Đang tải sơ đồ ghế...</span>
                        </div>
                      ) : seats.length === 0 ? (
                        <div className="text-center py-10 text-brand-muted text-sm">
                          Không có dữ liệu ghế cho suất chiếu này.
                        </div>
                      ) : (
                        <div className="grid gap-2 min-w-[380px]">
                          {/* Generate rows of seats - filter out invalid seatNumbers */}
                          {Array.from(new Set(
                            seats
                              .filter(s => s.seatNumber && s.seatNumber.length > 0)
                              .map(s => s.seatNumber[0])
                          )).sort().map((rowName) => {
                            const rowSeats = seats.filter(s => s.seatNumber && s.seatNumber.startsWith(rowName));
                            return (
                              <div key={rowName} className="flex items-center justify-between gap-2">
                                <span className="w-4 text-xs font-bold font-mono text-brand-muted text-center">{rowName}</span>
                                <div className="flex-1 flex justify-center gap-1">
                                  {rowSeats.map((seat) => {
                                    const isSelected = selectedSeats.some(s => s.id === seat.id);
                                    const isReserved = seat.status === 'RESERVED';
                                    
                                    return (
                                      <button
                                        key={seat.id}
                                        disabled={isReserved}
                                        onClick={() => handleSeatClick(seat)}
                                        className={`w-7 h-7 sm:w-8 sm:h-8 rounded text-[10px] sm:text-xs font-mono font-semibold transition-all duration-150 flex items-center justify-center cursor-pointer ${
                                          isReserved 
                                            ? 'bg-zinc-800 text-zinc-600 border border-zinc-900 cursor-not-allowed' 
                                            : isSelected 
                                              ? 'bg-brand-accent text-brand-bg shadow-[0_0_10px_rgba(229,169,59,0.3)] border border-brand-accent font-bold scale-105'
                                              : 'bg-brand-card hover:bg-zinc-800 text-brand-muted hover:text-white border border-brand-border'
                                        }`}
                                        title={`Ghế ${seat.seatNumber} - ${isReserved ? 'Đã đặt' : 'Còn trống'}`}
                                      >
                                        {seat.seatNumber.substring(1)}
                                      </button>
                                    );
                                  })}
                                </div>
                                <span className="w-4 text-xs font-bold font-mono text-brand-muted text-center">{rowName}</span>
                              </div>
                            );
                          })}
                        </div>
                      )}
                    </div>

                    {/* Seat Legend */}
                    <div className="flex justify-center gap-6 text-xs font-mono text-brand-muted border-t border-brand-border/60 pt-6 mt-4">
                      <div className="flex items-center gap-2">
                        <div className="w-4 h-4 rounded bg-brand-card border border-brand-border"></div>
                        <span>Còn trống</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <div className="w-4 h-4 rounded bg-brand-accent border border-brand-accent"></div>
                        <span>Đang chọn</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <div className="w-4 h-4 rounded bg-zinc-800 border border-zinc-900"></div>
                        <span>Đã đặt</span>
                      </div>
                    </div>
                  </motion.div>
                )}

                {/* Selection Summary Floating Panel / Bottom Card */}
                {selectedShowtime && selectedSeats.length > 0 && (
                  <motion.div 
                    initial={{ opacity: 0, y: 15 }}
                    animate={{ opacity: 1, y: 0 }}
                    className="bg-brand-card border border-brand-accent/30 rounded-2xl p-6 flex flex-col sm:flex-row justify-between items-center gap-6 cinematic-glow"
                  >
                    <div className="text-center sm:text-left">
                      <span className="text-xs text-brand-muted font-mono uppercase tracking-widest">Ghế đã chọn</span>
                      <div className="text-xl font-bold font-mono text-white mt-1">
                        {selectedSeats.map(s => s.seatNumber).join(', ')}
                      </div>
                      <div className="text-xs text-brand-muted font-mono mt-1">
                        Suất {selectedShowtime.startTime.substring(0, 5)} · {selectedShowtime.roomName}
                      </div>
                    </div>

                    <div className="flex items-center gap-6">
                      <div className="text-center sm:text-right">
                        <span className="text-xs text-brand-muted font-mono uppercase tracking-widest">Tổng cộng</span>
                        <div className="text-2xl font-bold font-mono text-brand-accent mt-0.5">
                          {((selectedShowtime.ticketPrice || selectedShowtime.price || 90000) * selectedSeats.length).toLocaleString()} đ
                        </div>
                      </div>

                      <button
                        onClick={handleConfirmBooking}
                        className="px-6 py-3.5 bg-brand-accent hover:bg-brand-accent-hover text-brand-bg font-bold rounded-xl transition-all duration-200 shadow-[0_4px_14px_rgba(229,169,59,0.3)] hover:-translate-y-0.5 cursor-pointer active:translate-y-0 active:scale-95"
                      >
                        Đặt Vé Ngay
                      </button>
                    </div>
                  </motion.div>
                )}
              </div>
            </div>
          </div>
        ) : (
          /* Movie List / Home Tab */
          <div className="space-y-12">
            {/* Spotlight Hero Movie */}
            {loading ? (
              <div className="h-[480px] w-full bg-brand-card/40 border border-brand-border rounded-3xl animate-pulse flex items-center justify-center">
                <FilmReel className="w-10 h-10 text-brand-accent animate-spin" />
              </div>
            ) : spotlightMovie ? (
              <motion.div 
                key={spotlightMovie.id}
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                transition={{ duration: 0.6 }}
                className="h-auto md:h-[485px] w-full rounded-3xl overflow-hidden border border-brand-border bg-brand-card/30 relative flex flex-col md:flex-row items-center justify-between p-6 sm:p-10 cinematic-glow"
              >
                {/* Visual Background Glow */}
                <div className="absolute inset-0 bg-cover bg-center opacity-10 blur-xl scale-110 pointer-events-none" style={{ backgroundImage: `url(${spotlightMovie.posterUrl})` }}></div>
                
                {/* Spotlight Info */}
                <div className="flex-1 max-w-xl z-10 text-center md:text-left mb-6 md:mb-0">
                  <span className="inline-flex items-center gap-1.5 px-3 py-1 bg-brand-accent/15 border border-brand-accent/30 text-brand-accent rounded-full text-xs font-mono font-bold mb-6">
                    <Sparkle className="w-3.5 h-3.5" /> SPOTLIGHT PHIM NỔI BẬT
                  </span>
                  
                  <h1 className="text-4xl md:text-5xl lg:text-6xl font-bold tracking-tighter text-white leading-none mb-4 max-w-[15ch]">
                    {spotlightMovie.title}
                  </h1>
                  
                  <div className="flex justify-center md:justify-start items-center gap-3 text-xs font-mono text-brand-muted mb-6">
                    <span className="px-2 py-0.5 bg-zinc-800 text-zinc-300 rounded font-bold">{spotlightMovie.rating || 'T13'}</span>
                    <span>·</span>
                    <span>{spotlightMovie.genre}</span>
                    <span>·</span>
                    <span>{spotlightMovie.duration} phút</span>
                  </div>

                  <p className="text-sm md:text-base text-brand-muted leading-relaxed mb-8 max-w-[45ch]">
                    {spotlightMovie.description}
                  </p>

                  <button 
                    onClick={() => setSelectedMovie(spotlightMovie)}
                    className="w-full sm:w-auto px-8 py-4 bg-brand-accent hover:bg-brand-accent-hover text-brand-bg font-bold rounded-xl transition-all duration-200 flex items-center justify-center gap-2 group cursor-pointer shadow-[0_4px_14px_rgba(229,169,59,0.2)] hover:-translate-y-0.5"
                  >
                    <span>Mua Vé Suất Chiếu</span>
                    <ArrowRight className="w-4 h-4 transition-transform group-hover:translate-x-1" />
                  </button>
                </div>

                {/* Spotlight Poster Block */}
                <div className="w-[240px] sm:w-[280px] aspect-[2/3] rounded-2xl overflow-hidden border border-brand-border/80 shadow-[0_8px_30px_rgb(0,0,0,0.6)] z-10 flex-shrink-0 relative group">
                  <img 
                    src={spotlightMovie.posterUrl || `https://picsum.photos/seed/${spotlightMovie.title}/400/600`} 
                    alt={spotlightMovie.title}
                    className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent pointer-events-none"></div>
                </div>
              </motion.div>
            ) : null}

            {/* Movie Bento Grid Selector */}
            <div>
              <div className="flex items-end justify-between mb-6">
                <div>
                  <span className="text-xs text-brand-accent font-mono uppercase tracking-widest block mb-1">Danh sách chọn lọc</span>
                  <h2 className="text-2xl font-bold tracking-tight text-white">Suất Chiếu Tại Rạp</h2>
                </div>
              </div>

              {loading ? (
                <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                  {Array.from({ length: 4 }).map((_, i) => (
                    <div key={i} className="aspect-[2/3] bg-brand-card/40 rounded-2xl animate-pulse border border-brand-border"></div>
                  ))}
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-12 gap-6">
                  {movies.map((movie, index) => {
                    // High variance layouts based on index
                    // Item 0 is featured wide card (col-span-8), Item 1 is medium vertical (col-span-4)
                    // Item 2 is medium vertical (col-span-4), Item 3 is wide horizontal (col-span-8)
                    const isColSpanLarge = index % 3 === 0;
                    const gridSpanClass = isColSpanLarge ? 'md:col-span-8' : 'md:col-span-4';

                    return (
                      <div 
                        key={movie.id}
                        onClick={() => setSelectedMovie(movie)}
                        className={`${gridSpanClass} bg-brand-card border border-brand-border/60 hover:border-brand-accent/30 rounded-3xl p-6 flex flex-col md:flex-row justify-between gap-6 cursor-pointer transition-all duration-300 group hover:shadow-[0_10px_30px_-10px_rgba(229,169,59,0.08)]`}
                      >
                        <div className="flex-1 flex flex-col justify-between">
                          <div>
                            <div className="flex items-center gap-2 mb-3">
                              <span className="px-2 py-0.5 bg-zinc-800 text-zinc-300 rounded text-[10px] font-mono font-bold">
                                {movie.rating || 'T13'}
                              </span>
                              <span className="text-[11px] text-brand-muted font-mono">{movie.duration} min</span>
                            </div>
                            <h3 className="text-xl font-bold text-white group-hover:text-brand-accent transition-colors mb-2 line-clamp-2">
                              {movie.title}
                            </h3>
                            <p className="text-xs text-brand-muted font-mono mb-4">{movie.genre}</p>
                            <p className="text-xs text-brand-muted leading-relaxed line-clamp-3 md:line-clamp-4">
                              {movie.description}
                            </p>
                          </div>

                          <div className="mt-4 md:mt-0 pt-4 flex items-center gap-1.5 text-xs text-brand-accent font-bold font-mono">
                            <span>ĐẶT VÉ NGAY</span>
                            <ArrowRight className="w-3.5 h-3.5 transition-transform group-hover:translate-x-1" />
                          </div>
                        </div>

                        {/* Image aspect ratio depends on span layout */}
                        <div className={`w-full md:w-[150px] lg:w-[180px] aspect-[2/3] rounded-2xl overflow-hidden flex-shrink-0 bg-zinc-900 border border-brand-border/40 relative`}>
                          <img 
                            src={movie.posterUrl || `https://picsum.photos/seed/${movie.title}/400/600`} 
                            alt={movie.title}
                            className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
                          />
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          </div>
        )}
      </main>

      {/* Transaction & Saga Polling Overlay Screen */}
      <AnimatePresence>
        {isBooking && (
          <motion.div 
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-50 bg-black/85 backdrop-blur-sm flex items-center justify-center p-6"
          >
            <div className="bg-brand-card border border-brand-border/80 rounded-3xl p-8 max-w-md w-full text-center cinematic-glow relative overflow-hidden">
              <div className="absolute top-0 left-0 w-full h-1.5 bg-zinc-800">
                <motion.div 
                  initial={{ width: 0 }}
                  animate={{ width: '100%' }}
                  transition={{ duration: 2.2, ease: "easeInOut" }}
                  className="h-full bg-brand-accent"
                />
              </div>

              <div className="w-16 h-16 bg-brand-accent/10 border border-brand-accent/30 rounded-full flex items-center justify-center mx-auto mb-6">
                <FilmReel className="w-8 h-8 text-brand-accent animate-spin" />
              </div>

              <h3 className="text-xl font-bold text-white mb-2">Đang Thiết Lập Giao Dịch</h3>
              
              {/* Saga Pattern logic explanation */}
              <div className="inline-flex items-center gap-1.5 px-3 py-1 bg-amber-500/10 border border-amber-500/20 text-amber-500 rounded-full text-[10px] font-mono mb-6">
                <Database className="w-3.5 h-3.5" /> Kafka Saga: Booking PENDING
              </div>

              <p className="text-sm text-brand-muted leading-relaxed mb-6">
                Hệ thống đang gửi yêu cầu đặt chỗ tạm thời qua Booking Service (Redis Lock) và thực hiện xác nhận thanh toán bất đồng bộ qua Payment Service. Vui lòng giữ kết nối.
              </p>

              {/* Polling Spinner detail */}
              <div className="flex items-center justify-center gap-2 text-xs font-mono text-brand-accent">
                <div className="w-2 h-2 bg-brand-accent rounded-full animate-bounce"></div>
                <div className="w-2 h-2 bg-brand-accent rounded-full animate-bounce delay-150"></div>
                <div className="w-2 h-2 bg-brand-accent rounded-full animate-bounce delay-300"></div>
                <span>Đang thăm dò kết quả (Polling)...</span>
              </div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Booking Outcome (Success or Error Screen) */}
      <AnimatePresence>
        {(bookingResult?.status === 'CONFIRMED' || bookingError) && (
          <motion.div 
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-50 bg-black/90 backdrop-blur-sm flex items-center justify-center p-6 overflow-y-auto"
          >
            <div className="bg-brand-bg/95 border border-brand-border rounded-3xl p-6 sm:p-8 max-w-2xl w-full relative cinematic-glow my-8">
              <button 
                onClick={handleResetAfterBooking}
                className="absolute top-4 right-4 text-brand-muted hover:text-white transition-colors cursor-pointer"
              >
                <X className="w-6 h-6" />
              </button>

              {bookingResult?.status === 'CONFIRMED' ? (
                /* Success Scenario */
                <div className="text-center">
                  <div className="w-12 h-12 bg-emerald-500/10 border border-emerald-500/30 rounded-full flex items-center justify-center mx-auto mb-4">
                    <ShieldCheck className="w-6 h-6 text-emerald-500" />
                  </div>
                  <h3 className="text-2xl font-bold text-white mb-2">Đặt vé thành công!</h3>
                  <div className="inline-flex items-center gap-1.5 px-3 py-1 bg-emerald-500/15 text-emerald-500 rounded-full text-xs font-mono mb-8">
                    Trạng thái: CONFIRMED (Saga Complete)
                  </div>

                  {/* Render Retro Ticket */}
                  <div className="text-left mb-8">
                    <RetroTicket 
                      booking={bookingResult} 
                      movie={selectedMovie || movies[0]} 
                    />
                  </div>

                  <div className="flex flex-col sm:flex-row gap-3 justify-center">
                    <button 
                      onClick={() => { handleResetAfterBooking(); setSelectedMovie(null); }}
                      className="px-6 py-3 bg-zinc-800 hover:bg-zinc-700 text-white font-semibold rounded-xl text-sm transition-colors cursor-pointer"
                    >
                      Đặt thêm phim khác
                    </button>
                    <button 
                      onClick={handleResetAfterBooking}
                      className="px-6 py-3 bg-brand-accent hover:bg-brand-accent-hover text-brand-bg font-bold rounded-xl text-sm transition-all cursor-pointer"
                    >
                      Xem sơ đồ ghế phòng chiếu
                    </button>
                  </div>
                </div>
              ) : (
                /* Failure Scenario */
                <div className="text-center py-6">
                  <div className="w-12 h-12 bg-red-500/10 border border-red-500/30 rounded-full flex items-center justify-center mx-auto mb-4">
                    <Warning className="w-6 h-6 text-red-500" />
                  </div>
                  <h3 className="text-xl font-bold text-white mb-2">Giao dịch thất bại</h3>
                  <div className="inline-flex items-center gap-1.5 px-3 py-1 bg-red-500/15 text-red-500 rounded-full text-xs font-mono mb-6">
                    Trạng thái: CANCELLED (Saga Rollback)
                  </div>
                  <p className="text-sm text-brand-muted max-w-md mx-auto mb-8 leading-relaxed">
                    {bookingError || "Một lỗi xảy ra khi xác thực thanh toán. Các ghế bạn đã đăng ký giữ tạm thời (Redis Lock) đã được giải phóng tự động về trạng thái Khả dụng."}
                  </p>
                  <button 
                    onClick={handleResetAfterBooking}
                    className="px-6 py-3 bg-brand-accent hover:bg-brand-accent-hover text-brand-bg font-bold rounded-xl text-sm transition-colors cursor-pointer"
                  >
                    Quay lại Sơ Đồ Ghế
                  </button>
                </div>
              )}
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Footer */}
      <footer className="border-t border-brand-border/60 bg-brand-bg/50 py-12 mt-12 text-xs text-brand-muted font-mono">
        <div className="max-w-[1400px] mx-auto px-6 flex flex-col md:flex-row justify-between items-center gap-6">
          <div>
            <span className="text-brand-accent font-bold tracking-wider">NOVA CINE</span> · Premium Cinema Ticketing Platform
          </div>
          <div className="flex gap-6">
            <a href="#" className="hover:text-white transition-colors">Điều khoản sử dụng</a>
            <a href="#" className="hover:text-white transition-colors">Chính sách bảo mật</a>
            <a href="#" className="hover:text-white transition-colors">Hệ thống phòng chiếu</a>
          </div>
          <div>
            © 2026 NovaCine Group. Built with Taste-Skill design principles.
          </div>
        </div>
      </footer>
    </div>
  );
}

// Retro Ticket Pass Component
interface RetroTicketProps {
  booking: Booking;
  movie: Movie;
}

function RetroTicket({ booking, movie }: RetroTicketProps) {
  // Hardcoded showtime lookup for formatting details since showtime object isn't fully linked
  const dateStr = booking.createdAt ? new Date(booking.createdAt).toLocaleDateString('vi-VN', {
    weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
  }) : "Thứ Sáu, ngày 5 tháng 6 năm 2026";

  const downloadTicket = () => {
    alert("Tính năng tải vé đang chuẩn bị: PDF của bạn sẽ được tải xuống tự động chứa mã QR này.");
  };

  return (
    <div className="w-full max-w-xl mx-auto bg-brand-card border border-brand-border rounded-2xl overflow-hidden shadow-2xl relative flex flex-col md:flex-row select-none">
      {/* Visual Perforation Dots on Left/Right for Retro look */}
      <div className="hidden md:block absolute left-[70%] top-0 bottom-0 w-[1px] border-r border-dashed border-brand-border/80"></div>
      <div className="hidden md:block absolute left-[70%] -top-3 w-6 h-6 rounded-full bg-brand-bg border border-brand-border"></div>
      <div className="hidden md:block absolute left-[70%] -bottom-3 w-6 h-6 rounded-full bg-brand-bg border border-brand-border"></div>

      {/* Ticket Main Info Body */}
      <div className="flex-1 p-6 flex flex-col justify-between">
        <div>
          <div className="flex justify-between items-start gap-4 mb-4">
            <div>
              <span className="text-[10px] text-brand-accent font-mono uppercase tracking-widest font-bold">VÉ XEM PHIM CHÍNH THỨC</span>
              <h4 className="text-xl font-bold text-white tracking-tight mt-1 line-clamp-1">{movie.title}</h4>
            </div>
            <span className="px-2 py-0.5 bg-zinc-800 text-zinc-300 rounded text-[9px] font-mono font-bold mt-1">
              {movie.rating || 'T13'}
            </span>
          </div>

          <div className="grid grid-cols-2 gap-y-4 gap-x-6 text-xs font-mono text-brand-muted mb-6">
            <div>
              <span className="text-[9px] uppercase tracking-wider block text-brand-muted/70 mb-0.5">NGÀY CHIẾU</span>
              <span className="text-white font-semibold">{dateStr}</span>
            </div>
            <div>
              <span className="text-[9px] uppercase tracking-wider block text-brand-muted/70 mb-0.5">PHÒNG CHIẾU</span>
              <span className="text-white font-semibold">ROOM 1 (IMAX)</span>
            </div>
            <div>
              <span className="text-[9px] uppercase tracking-wider block text-brand-muted/70 mb-0.5">SỐ GHẾ</span>
              <span className="text-brand-accent font-bold text-sm">
                {booking.seats.map(s => s.seatNumber).join(', ')}
              </span>
            </div>
            <div>
              <span className="text-[9px] uppercase tracking-wider block text-brand-muted/70 mb-0.5">TỔNG TIỀN VÉ</span>
              <span className="text-white font-semibold">{(booking.totalPrice || 90000).toLocaleString()} đ</span>
            </div>
          </div>
        </div>

        <div className="border-t border-brand-border/60 pt-4 flex justify-between items-center">
          <div className="text-[9px] font-mono text-brand-muted">
            <div>MÃ GIAO DỊCH</div>
            <div className="text-white font-bold tracking-wider">{booking.id}</div>
          </div>
          <button 
            onClick={downloadTicket}
            className="p-2 bg-zinc-800 hover:bg-zinc-700 text-brand-accent rounded-lg transition-colors cursor-pointer" 
            title="Tải vé về máy"
          >
            <Download className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Ticket QR Stub (Right col on desktop, bottom on mobile) */}
      <div className="w-full md:w-[30%] bg-zinc-900/50 p-6 flex flex-col items-center justify-center border-t md:border-t-0 md:border-l border-brand-border/60">
        <div className="w-28 h-28 bg-white p-2.5 rounded-xl border border-zinc-800 flex items-center justify-center shadow-lg">
          {/* Simulated QR Code using CSS */}
          <div className="w-full h-full bg-cover" style={{ backgroundImage: `url('https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=${booking.id}')` }}></div>
        </div>
        <span className="text-[9px] font-mono text-brand-muted mt-3 tracking-widest">NOVA ENTRY QR</span>
      </div>
    </div>
  );
}

// Fallback constant just for React safety
const MOCK_MOVIES_FALLBACK = [
  {
    id: "bd298426-ef40-441a-9b21-3d20bfffa316",
    title: "Lat Mat 7: Mot Dieu Uoc",
    genre: "Drama/Family",
    duration: 138,
    releaseDate: "2026-06-05",
    posterUrl: "/images/lat-mat.jpg",
    rating: "T13"
  }
];
