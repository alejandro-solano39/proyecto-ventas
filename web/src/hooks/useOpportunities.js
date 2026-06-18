import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '../services/api';

export function useOpportunities(filters = {}) {
  return useQuery({
    queryKey: ['opportunities', filters],
    queryFn: async () => {
      const { data } = await api.get('/opportunities', { params: filters });
      return data;
    },
    refetchInterval: 5000, // polling cada 5 segundos
  });
}

export function useOpportunityDetail(id) {
  return useQuery({
    queryKey: ['opportunity', id],
    queryFn: async () => {
      const { data } = await api.get(`/opportunities/${id}`);
      return data;
    },
    enabled: !!id,
  });
}

export function useUpdateOpportunity() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, ...updates }) => api.patch(`/opportunities/${id}`, updates),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['opportunities'] });
    },
  });
}

export function useDeleteOpportunity() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id) => api.delete(`/opportunities/${id}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['opportunities'] });
    },
  });
}
