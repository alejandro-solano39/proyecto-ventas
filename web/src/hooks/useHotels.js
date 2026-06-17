import { useQuery } from '@tanstack/react-query';
import api from '../services/api';

export function useHotels(city) {
  return useQuery({
    queryKey: ['hotels', city],
    queryFn: async () => {
      const { data } = await api.get('/hotels', { params: city ? { city } : {} });
      return data.hotels;
    },
    staleTime: 60000, // hoteles cambian poco
  });
}
