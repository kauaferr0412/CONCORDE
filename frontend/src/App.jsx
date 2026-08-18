import { Navigate, Route, Routes } from "react-router-dom";
import { useAuth } from "./context/AuthContext.jsx";
import { ProfileProvider } from "./context/ProfileContext.jsx";
import LoginPage from "./pages/LoginPage.jsx";
import ServerPage from "./pages/ServerPage.jsx";
import AdminPage from "./pages/AdminPage.jsx";

function PrivateRoute({ children }) {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? children : <Navigate to="/login" replace />;
}

export default function App() {
  return (
    <ProfileProvider>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route
          path="/admin"
          element={
            <PrivateRoute>
              <AdminPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/servers/:serverId?"
          element={
            <PrivateRoute>
              <ServerPage />
            </PrivateRoute>
          }
        />
        <Route path="*" element={<Navigate to="/servers" replace />} />
      </Routes>
    </ProfileProvider>
  );
}
