import { fetchCurrentUser } from "@/api/auth"
import { useQuery } from "@tanstack/react-query"


// Cache key of the signed-in user; logout removes it, so the next page asks the server again.
export const CURRENT_USER_KEY = ['currentUser']

/** The signed-in user from GET /api/users/me; fails with a 401 ApiError when nobody is signed in. */
export function useCurrentUser(){
    return useQuery({
        queryKey: CURRENT_USER_KEY,
        queryFn:fetchCurrentUser,
        retry: false, 
        staleTime: 5 * 60 * 1000, 
    })

}