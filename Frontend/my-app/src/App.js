import React, {useContext} from 'react';
import './App.css';
import LogView from "./components/LogView";
import {BrowserRouter as Router, Link as RouterLink, Route, Routes} from 'react-router-dom';
import {AppBar, Box, Button, Container, IconButton, List, ListItem, Paper, Toolbar, Typography} from '@mui/material';

function App() {
    return (
            <Router>
                <Layout />
            </Router>
    );
}

function  Layout() {
    return(
        <>
            <AppBar position="static" color="primary" sx={{ minHeight: 64 }}>
                <Toolbar>
                    <Typography variant="h6" sx={{ flexGrow: 1 }}>
                        React Logs Viewer
                    </Typography>
                    <Button color="inherit" component={RouterLink} to="/">
                        Home
                    </Button>
                    <Button color="inherit" component={RouterLink} to="/logs">
                        View Logs
                    </Button>
                </Toolbar>
            </AppBar>

            <Container sx={{ mt: 8, mb: 12 }}>
                <Routes>
                    <Route path="/logs" element={<LogView />} />
                </Routes>
            </Container>

            <Container maxWidth="lg">
                <Box my={4}>
                    <Typography variant="h6">About The Project</Typography>
                    <Paper elevation={3} style={{padding: '20px', marginTop: '10px'}}>
                        <Typography variant="body1">
                            We're not just processing logs; we're embarking on a data expedition, uncovering
                            patterns and insights with every step. This project is where efficiency meets
                            excitement, and where data analysis gets a dose of dynamism!
                        </Typography>
                    </Paper>
                </Box>

                <Box my={4}>
                    <Typography variant="h6">Built With</Typography>
                    <List>
                        <ListItem>Spring Boot</ListItem>
                        <ListItem>React</ListItem>
                        <ListItem>Docker & Docker Compose</ListItem>
                        <ListItem>Elasticsearch & ScyllaDB</ListItem>
                        <ListItem>Kafka & Redis</ListItem>
                        <ListItem>PostgreSQL</ListItem>
                    </List>
                </Box>

                <Box my={4}>
                    <Typography variant="h6">Getting Started</Typography>
                    <Paper elevation={3} style={{padding: '20px', marginTop: '10px'}}>
                        <Typography variant="body1">
                            Your adventurer's hat and a zest for data!
                            <br/>
                            Docker and Docker Compose, your trusty tools for this journey.
                            <br/>
                            JDK 17 and the latest React, because staying updated is key.
                        </Typography>
                    </Paper>
                </Box>

                <Box my={4}>
                    <Typography variant="body2" color="textSecondary" align="center">
                        {'Copyright © '}
                        {new Date().getFullYear()}
                        {' '}
                        Log Analysis System. All rights reserved.
                    </Typography>
                </Box>
            </Container>
        </>
    );
}

export default App;
