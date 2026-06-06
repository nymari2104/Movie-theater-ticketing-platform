import axios from 'axios';

// Types representing the backend schemas
export interface Movie {
  id: string;
  title: string;
  genre: string;
  duration: number; // in minutes
  releaseDate: string;
  posterUrl?: string;
  backdropUrl?: string;
  description?: string;
  rating?: string;
}

export interface Showtime {
  id: string;
  movieId: string;
  showDate: string;
  startTime: string;
  endTime: string;
  roomName: string;
  ticketPrice?: number;
  price?: number;
}

export interface Seat {
  id: string;
  showtimeId: string;
  seatNumber: string;
  status: 'AVAILABLE' | 'RESERVED';
}

export interface BookingSeat {
  id: string;
  bookingId: string;
  seatId: string;
  seatNumber: string;
  bookingDate: string;
}

export interface Booking {
  id: string;
  userId: string;
  showtimeId: string;
  totalPrice: number;
  status: 'PENDING' | 'CONFIRMED' | 'CANCELLED';
  createdAt: string;
  seats: BookingSeat[];
}

// Global configuration
const API_BASE = '/api';
let isMockingEnabled = false;

// Mock Store for Simulated Saga Pattern
const mockBookingsStore: Record<string, Booking> = {};

// Hardcoded mock data with cinematic enrichment
const MOCK_MOVIES: Movie[] = [
  {
    id: "bd298426-ef40-441a-9b21-3d20bfffa316",
    title: "Lat Mat 7: Mot Dieu Uoc",
    genre: "Drama/Family",
    duration: 138,
    releaseDate: "2026-06-05",
    posterUrl: "/images/lat-mat.jpg",
    backdropUrl: "/images/lat-mat-bg.jpg",
    description: "A touching story about maternal love and family bonds. An elderly mother of five children goes through emotional trials and triumphs as she waits for her children to fulfill her one and only wish.",
    rating: "T13"
  },
  {
    id: "dune-2-id-uuid-1111",
    title: "Dune: Part Two",
    genre: "Sci-Fi/Adventure",
    duration: 166,
    releaseDate: "2026-03-01",
    posterUrl: "/images/dune.jpg",
    backdropUrl: "/images/dune-bg.jpg",
    description: "Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family. Facing a choice between the love of his life and the fate of the universe, he endeavors to prevent a terrible future.",
    rating: "T16"
  },
  {
    id: "oppenheimer-id-uuid-2222",
    title: "Oppenheimer",
    genre: "Biography/Drama",
    duration: 180,
    releaseDate: "2026-07-21",
    posterUrl: "/images/oppenheimer.jpg",
    backdropUrl: "/images/oppenheimer-bg.jpg",
    description: "The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb, illustrating the brilliant mind that shaped modern warfare and the moral conflicts that followed.",
    rating: "T18"
  },
  {
    id: "lotr-id-uuid-3333",
    title: "The Lord of the Rings: The Fellowship of the Ring",
    genre: "Fantasy/Adventure",
    duration: 178,
    releaseDate: "2026-12-18",
    posterUrl: "/images/lotr.jpg",
    backdropUrl: "/images/lotr-bg.jpg",
    description: "A meek Hobbit from the Shire and eight companions set out on a journey to destroy the powerful One Ring and save Middle-earth from the Dark Lord Sauron.",
    rating: "T13"
  }
];

const MOCK_SHOWTIMES: Record<string, Showtime[]> = {
  "bd298426-ef40-441a-9b21-3d20bfffa316": [
    { id: "st-lm-1", movieId: "bd298426-ef40-441a-9b21-3d20bfffa316", showDate: "2026-06-05", startTime: "10:00:00", endTime: "12:18:00", roomName: "Room 1 (IMAX)", ticketPrice: 120000 },
    { id: "3043a97e-dab1-48ed-a3ee-08663959ed6d", movieId: "bd298426-ef40-441a-9b21-3d20bfffa316", showDate: "2026-06-05", startTime: "19:00:00", endTime: "21:18:00", roomName: "Room 2", ticketPrice: 90000 },
    { id: "st-lm-3", movieId: "bd298426-ef40-441a-9b21-3d20bfffa316", showDate: "2026-06-05", startTime: "22:30:00", endTime: "00:48:00", roomName: "Room 3 (Standard)", ticketPrice: 85000 }
  ],
  "dune-2-id-uuid-1111": [
    { id: "st-dune-1", movieId: "dune-2-id-uuid-1111", showDate: "2026-06-05", startTime: "13:00:00", endTime: "15:46:00", roomName: "Room 1 (IMAX)", ticketPrice: 130000 },
    { id: "st-dune-2", movieId: "dune-2-id-uuid-1111", showDate: "2026-06-05", startTime: "18:30:00", endTime: "21:16:00", roomName: "Room 1 (IMAX)", ticketPrice: 130000 },
    { id: "st-dune-3", movieId: "dune-2-id-uuid-1111", showDate: "2026-06-05", startTime: "21:30:00", endTime: "00:16:00", roomName: "Room 2", ticketPrice: 95000 }
  ],
  "oppenheimer-id-uuid-2222": [
    { id: "st-opp-1", movieId: "oppenheimer-id-uuid-2222", showDate: "2026-06-05", startTime: "11:00:00", endTime: "14:00:00", roomName: "Room 2", ticketPrice: 95000 },
    { id: "st-opp-2", movieId: "oppenheimer-id-uuid-2222", showDate: "2026-06-05", startTime: "15:30:00", endTime: "18:30:00", roomName: "Room 3 (Standard)", ticketPrice: 85000 },
    { id: "st-opp-3", movieId: "oppenheimer-id-uuid-2222", showDate: "2026-06-05", startTime: "20:00:00", endTime: "23:00:00", roomName: "Room 1 (IMAX)", ticketPrice: 130000 }
  ],
  "lotr-id-uuid-3333": [
    { id: "st-lotr-1", movieId: "lotr-id-uuid-3333", showDate: "2026-06-05", startTime: "14:30:00", endTime: "17:28:00", roomName: "Room 2", ticketPrice: 90000 },
    { id: "st-lotr-2", movieId: "lotr-id-uuid-3333", showDate: "2026-06-05", startTime: "19:15:00", endTime: "22:13:00", roomName: "Room 3 (Standard)", ticketPrice: 85000 }
  ]
};

// Generate standard seat maps (A-F, 1-10)
const generateMockSeats = (showtimeId: string): Seat[] => {
  const seats: Seat[] = [];
  const rows = ['A', 'B', 'C', 'D', 'E', 'F'];
  
  // Deterministic seed for seat occupancy based on showtime ID
  let hash = 0;
  for (let i = 0; i < showtimeId.length; i++) {
    hash = showtimeId.charCodeAt(i) + ((hash << 5) - hash);
  }

  rows.forEach((row, rowIndex) => {
    for (let num = 1; num <= 10; num++) {
      const seatNumber = `${row}${num}`;
      const seatId = `seat-${showtimeId}-${seatNumber}`;
      
      // Determine occupancy status using deterministic pseudo-random logic
      // Row F (back) and middle columns are more likely to be reserved
      const seatSeed = (hash + rowIndex * 10 + num) % 100;
      const isReserved = seatSeed < 30 || (rowIndex >= 3 && num >= 4 && num <= 7 && seatSeed < 65);

      seats.push({
        id: seatId,
        showtimeId,
        seatNumber,
        status: isReserved ? 'RESERVED' : 'AVAILABLE'
      });
    }
  });

  return seats;
};

// Simulated store for mock seats status updates (so booked seats remain booked)
const mockSeatsStore: Record<string, Seat[]> = {};

// Helper to extract result from ApiResponse wrapper { code, message, data }
const extractResult = (response: any) => {
  const data = response.data;
  if (data === undefined || data === null) return null;
  // Handles { code: "SUCCESS", message: "...", data: [...] }
  if (data.code !== undefined) {
    if (data.data !== undefined) return data.data;
    if (data.result !== undefined) return data.result;
    return null;
  }
  return data;
};

// Helper to parse LocalTime from Jackson - can be "HH:mm:ss" string or [H, M, S] array
const parseLocalTime = (time: any): string => {
  if (!time) return '00:00:00';
  if (typeof time === 'string') return time;
  // Jackson may serialize LocalTime as array [hour, minute, second]
  if (Array.isArray(time)) {
    const [h, m, s = 0] = time;
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
  }
  return String(time);
};

// Helper to parse LocalDate - can be "YYYY-MM-DD" or [YYYY, MM, DD] array
const parseLocalDate = (date: any): string => {
  if (!date) return '';
  if (typeof date === 'string') return date;
  if (Array.isArray(date)) {
    const [y, m, d] = date;
    return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
  }
  return String(date);
};

// Helper to determine if we need to fall back to mock
const checkFallback = (error: any) => {
  console.warn('API Error encountered, falling back to mock data:', error.message || error);
  isMockingEnabled = true;
  return isMockingEnabled;
};

// Convert posterBase64 to a usable image src
const buildPosterSrc = (posterBase64: string | null | undefined, fallbackSeed: string): string => {
  if (posterBase64 && posterBase64.trim().length > 0) {
    // If it already is a data URI or URL, return as-is
    if (posterBase64.startsWith('data:') || posterBase64.startsWith('http')) {
      return posterBase64;
    }
    return `data:image/jpeg;base64,${posterBase64}`;
  }
  return `https://picsum.photos/seed/${encodeURIComponent(fallbackSeed)}/400/600`;
};

export const api = {
  isMockMode: () => isMockingEnabled,
  enableMock: (enable: boolean) => {
    isMockingEnabled = enable;
  },

  getMovies: async (): Promise<Movie[]> => {
    if (isMockingEnabled) {
      return MOCK_MOVIES;
    }
    try {
      const response = await axios.get(`${API_BASE}/events/movies`);
      const result = extractResult(response);
      const apiMovies = Array.isArray(result) ? result : [];
      if (apiMovies.length === 0) {
        console.warn('API returned empty movie list. Automatically switching to mock mode.');
        isMockingEnabled = true;
        return MOCK_MOVIES;
      }
      return apiMovies.map((m: any) => {
        // Try to find enrichment data from local mock store by title match
        const fallback: Partial<Movie> = MOCK_MOVIES.find(
          fm => fm.title.toLowerCase() === (m.title || '').toLowerCase()
        ) || {};
        const idStr = m.id?.toString() || String(Math.random());
        return {
          id: idStr,
          title: m.title || 'Unknown Movie',
          genre: m.genre || fallback.genre || 'Drama',
          // Backend field is durationMinutes, not duration
          duration: m.durationMinutes || m.duration || fallback.duration || 120,
          releaseDate: parseLocalDate(m.releaseDate),
          // Backend stores image as posterBase64 bytes
          posterUrl: buildPosterSrc(m.posterBase64, m.title || idStr),
          backdropUrl: fallback.backdropUrl || `https://picsum.photos/seed/${encodeURIComponent((m.title || idStr) + 'bg')}/1200/800`,
          description: m.description || fallback.description || 'Enjoy the cinematic experience.',
          rating: m.rating || fallback.rating || 'T13'
        };
      });
    } catch (error) {
      checkFallback(error);
      return MOCK_MOVIES;
    }
  },

  getShowtimes: async (movieId: string, showDate: string): Promise<Showtime[]> => {
    if (isMockingEnabled) {
      return MOCK_SHOWTIMES[movieId] || [];
    }
    try {
      const response = await axios.get(`${API_BASE}/events/showtimes`, {
        params: { movieId, showDate }
      });
      const result = extractResult(response);
      if (!Array.isArray(result)) return [];
      return result.map((st: any) => ({
        id: st.id?.toString() || String(Math.random()),
        movieId: st.movieId?.toString() || movieId,
        showDate: parseLocalDate(st.showDate),
        startTime: parseLocalTime(st.startTime),
        endTime: parseLocalTime(st.endTime),
        roomName: st.roomName || 'Room 1',
        // Backend uses 'price' as BigDecimal which comes as a number
        ticketPrice: st.price ? Number(st.price) : (st.ticketPrice ? Number(st.ticketPrice) : 90000),
        price: st.price ? Number(st.price) : (st.ticketPrice ? Number(st.ticketPrice) : 90000),
      }));
    } catch (error) {
      checkFallback(error);
      return MOCK_SHOWTIMES[movieId] || [];
    }
  },

  getSeats: async (showtimeId: string): Promise<Seat[]> => {
    if (isMockingEnabled) {
      if (!mockSeatsStore[showtimeId]) {
        mockSeatsStore[showtimeId] = generateMockSeats(showtimeId);
      }
      return mockSeatsStore[showtimeId];
    }
    try {
      const response = await axios.get(`${API_BASE}/events/showtimes/${showtimeId}/seats`);
      const result = extractResult(response);
      if (!Array.isArray(result) || result.length === 0) {
        // If API returns no seats, generate mock seats so UI is not blank
        if (!mockSeatsStore[showtimeId]) {
          mockSeatsStore[showtimeId] = generateMockSeats(showtimeId);
        }
        return mockSeatsStore[showtimeId];
      }
      return result.map((s: any) => ({
        id: s.id?.toString() || String(Math.random()),
        showtimeId: s.showtimeId?.toString() || showtimeId,
        seatNumber: s.seatNumber || 'A1',
        status: (s.status === 'RESERVED' || s.status === 'LOCKED') ? 'RESERVED' : 'AVAILABLE'
      }));
    } catch (error) {
      checkFallback(error);
      if (!mockSeatsStore[showtimeId]) {
        mockSeatsStore[showtimeId] = generateMockSeats(showtimeId);
      }
      return mockSeatsStore[showtimeId];
    }
  },

  createBooking: async (bookingData: {
    userId: string;
    showtimeId: string;
    seatIds: string[];
    seatNumbers: string[];
    bookingDate: string;
    totalPrice: number;
  }): Promise<Booking> => {
    if (isMockingEnabled) {
      const bookingId = `bk-${Math.random().toString(36).substr(2, 9)}`;
      const newBooking: Booking = {
        id: bookingId,
        userId: bookingData.userId,
        showtimeId: bookingData.showtimeId,
        totalPrice: bookingData.totalPrice,
        status: 'PENDING',
        createdAt: new Date().toISOString(),
        seats: bookingData.seatIds.map((seatId, idx) => ({
          id: `bks-${Math.random().toString(36).substr(2, 9)}`,
          bookingId,
          seatId,
          seatNumber: bookingData.seatNumbers[idx],
          bookingDate: bookingData.bookingDate
        }))
      };

      // Store booking in state
      mockBookingsStore[bookingId] = newBooking;

      // Update seat status in mock store
      const seats = mockSeatsStore[bookingData.showtimeId] || [];
      mockSeatsStore[bookingData.showtimeId] = seats.map(s => {
        if (bookingData.seatIds.includes(s.id)) {
          return { ...s, status: 'RESERVED' };
        }
        return s;
      });

      // Simulate Saga transaction: transition status after 2 seconds
      // 85% success rate, 15% fail rate
      setTimeout(() => {
        const booking = mockBookingsStore[bookingId];
        if (booking && booking.status === 'PENDING') {
          const isSuccess = Math.random() < 0.85;
          booking.status = isSuccess ? 'CONFIRMED' : 'CANCELLED';
          
          // If cancelled, release seats
          if (!isSuccess) {
            const seats = mockSeatsStore[bookingData.showtimeId] || [];
            mockSeatsStore[bookingData.showtimeId] = seats.map(s => {
              if (bookingData.seatIds.includes(s.id)) {
                return { ...s, status: 'AVAILABLE' };
              }
              return s;
            });
          }
          console.log(`Mock Saga Transaction Completed for ${bookingId}:`, booking.status);
        }
      }, 2500);

      return newBooking;
    }

    try {
      const response = await axios.post(`${API_BASE}/bookings`, bookingData);
      return extractResult(response);
    } catch (error) {
      checkFallback(error);
      // Retry in mock mode
      return api.createBooking(bookingData);
    }
  },

  getBookingDetails: async (bookingId: string): Promise<Booking> => {
    if (isMockingEnabled) {
      const booking = mockBookingsStore[bookingId];
      if (!booking) {
        throw new Error('Booking not found in mock store');
      }
      return booking;
    }
    try {
      const response = await axios.get(`${API_BASE}/bookings/${bookingId}`);
      return extractResult(response);
    } catch (error) {
      checkFallback(error);
      return mockBookingsStore[bookingId] || (() => { throw new Error('Booking not found') })();
    }
  }
};
