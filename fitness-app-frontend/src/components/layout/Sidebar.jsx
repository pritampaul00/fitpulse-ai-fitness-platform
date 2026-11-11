// import { NavLink } from "react-router";

// const navigation = [
//     { label: "Dashboard", path: "/dashboard", icon: "⌂" },
//     { label: "Activities", path: "/activities", icon: "◷" },
//     { label: "AI Coach", path: "/coach", icon: "✧" },
// ];

// const Sidebar = () => {
//     return (
//         <aside className="app-sidebar">
//             <div className="sidebar-brand">
//                 <div className="brand-mark">F</div>
//                 <div>
//                     <h2>FitPulse</h2>
//                     <span>AI FITNESS PLATFORM</span>
//                 </div>
//             </div>

//             <div className="sidebar-section-label">
//                 WORKSPACE
//             </div>

//             <nav className="sidebar-navigation">
//                 {navigation.map((item) => (
//                     <NavLink
//                         key={item.path}
//                         to={item.path}
//                         className={({ isActive }) =>
//                             `sidebar-link ${isActive ? "active" : ""}`
//                         }
//                     >
//                         <span className="sidebar-icon">
//                             {item.icon}
//                         </span>
//                         {item.label}
//                     </NavLink>
//                 ))}
//             </nav>

//             <div className="sidebar-bottom">
//                 <div className="sidebar-coach-card">
//                     <span className="coach-card-icon">✧</span>
//                     <strong>Your AI Coach</strong>
//                     <p>Get personalized fitness insights.</p>
//                     <NavLink to="/coach">
//                         Start chatting →
//                     </NavLink>
//                 </div>
//             </div>
//         </aside>
//     );
// };

// export default Sidebar;


import { NavLink } from "react-router";

const navigation = [
    { label: "Dashboard", path: "/dashboard", icon: "⌂" },
    { label: "Activities", path: "/activities", icon: "◷" },
    { label: "Workout Planner", path: "/workouts", icon: "▦" },
    { label: "Goals", path: "/goals", icon: "◎" },
    { label: "AI Coach", path: "/coach", icon: "✧" },
];

const Sidebar = () => {
    return (
        <aside className="app-sidebar">
            <div className="sidebar-brand">
                <div className="brand-mark">F</div>
                <div>
                    <h2>FitPulse</h2>
                    <span>AI FITNESS PLATFORM</span>
                </div>
            </div>

            <div className="sidebar-section-label">
                WORKSPACE
            </div>

            <nav className="sidebar-navigation">
                {navigation.map((item) => (
                    <NavLink
                        key={item.path}
                        to={item.path}
                        className={({ isActive }) =>
                            `sidebar-link ${isActive ? "active" : ""}`
                        }
                    >
                        <span className="sidebar-icon">
                            {item.icon}
                        </span>
                        {item.label}
                    </NavLink>
                ))}
            </nav>

            <div className="sidebar-bottom">
                <div className="sidebar-coach-card">
                    <span className="coach-card-icon">✧</span>
                    <strong>Your AI Coach</strong>
                    <p>Get personalized fitness insights.</p>
                    <NavLink to="/coach">
                        Start chatting →
                    </NavLink>
                </div>
            </div>
        </aside>
    );
};

export default Sidebar;