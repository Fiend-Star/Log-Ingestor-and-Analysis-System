import React, { createContext, useState } from 'react';
import { createTheme, ThemeProvider as MUIThemeProvider } from '@mui/material/styles';

export const ThemeContext = createContext();

export const ThemeProvider = ({ children }) => {
    const [darkMode, setDarkMode] = useState('');

    const theme = createTheme({
        palette: {
            mode: darkMode ? 'dark' : 'light',
            // other theme settings
        },
    });

    return (
        <ThemeContext.Provider value={{ darkMode, setDarkMode }}>
            <MUIThemeProvider theme={theme}>
                {children}
            </MUIThemeProvider>
        </ThemeContext.Provider>
    );
};