import { createContext, useContext, useEffect, useState } from "react";
import axiosApi from "../api/AxiosApi";

const UserContext = createContext();

export const UserProvider = ({ children }) => {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [user, setUser] = useState(null);

    const login = async (userData) => {
        const data = await axiosApi.post('/auth/login', userData);
        if(data) {
            setIsLoggedIn(true);
            setUser(data?.email);
        }
    }

    const logout = () => {
        setIsLoggedIn(false);
        setUser(null);
        axiosApi.post('/auth/logout');
    }

    const checkInfo = async () => {
        try {
            const responseData = await axiosApi.get('/auth/myInfo');
            setIsLoggedIn(true);
            setUser(responseData);
        } catch (error) {
            setIsLoggedIn(false);
            setUser(null);
        }
    }

    useEffect(() => {
        checkInfo();
    }, []);

    const contextValue = {
        isLoggedIn,
        user,
        login,
        logout,
        checkInfo,
    }

    return (
        <UserContext.Provider value={contextValue}>
            {children}
        </UserContext.Provider>
    )
}

export const useUser = () => {
    return useContext(UserContext);
}