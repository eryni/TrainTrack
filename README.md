# TrainTrack
AI-Based Passenger Density Prediction System in MRT/LRT to Optimize Commuter Waiting and Travel Choices

## Project Overview
TrainTrack is a web-based platform that provides AI-powered predictions of passenger density for Metro Manila's MRT-3 system, which enable commuters to make informed travel decisions and optimize their commute experience.

## Project Structure
```
/TrainTrack
  /TrainTrackAngular   # Angular frontend application
  /TrainTrackAPI       # Spring Boot backend API
  README.md            # Project documentation
```

## Features
- **Real-time Predictions**: View predicted passenger density (Light/Moderate/Heavy/Very Heavy) for specific stations and times
- **User Authentication**: Secure sign-up and login system with email verification
- **Saved Stations**: Save and manage preferred MRT-3 stations
- **Prediction History**: Track your previous searches and predictions
- **Power BI Integration**: Interactive visualizations of ridership trends and historical data
- **Issue Reporting**: Report problems or feedback directly through the platform
- **Notifications**: Receive alerts and updates about system changes

## Technology Stack

### Frontend (TrainTrackAngular)
- **Framework**: Angular 16+
- **UI Libraries**: Bootstrap, Font Awesome
- **Routing**: Angular Router
- **HTTP Client**: Angular HttpClient

### Backend (TrainTrackAPI)
- **Framework**: Spring Boot 3.x
- **Language**: Java 17+
- **Database**: MySQL/PostgreSQL
- **Security**: Spring Security with JWT
- **ORM**: Spring Data JPA

## Database Schema
The system uses 9 interconnected tables:
- **User**: User account information
- **Admin**: Administrator accounts
- **Station**: MRT-3 station data (13 stations)
- **Train**: Current train positions and status
- **Prediction**: AI-generated passenger density forecasts
- **SavedStation**: User's bookmarked stations
- **PredictionHistory**: User's search history
- **Notification**: System alerts and messages
- **ReportedIssue**: User-submitted issues and feedback

## Getting Started

### Prerequisites
- Node.js 16+ and npm
- Angular CLI (`npm install -g @angular/cli`)
- Java JDK 17+
- Maven 3.6+
- MySQL 8+ or PostgreSQL 14+

### Frontend Setup (TrainTrackAngular)
```bash
# Navigate to Angular app
cd TrainTrackAngular

# Install dependencies
npm install

# Start development server
ng serve

# Access at http://localhost:4200
```

### Backend Setup (TrainTrackAPI)
```bash
# Navigate to Spring Boot API
cd TrainTrackAPI

# Configure database connection in application.properties
# src/main/resources/application.properties

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run

# API available at http://localhost:8080
```

### Database Configuration
Update `TrainTrackAPI/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/traintrack_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

## Key API Endpoints

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout

### Stations
- `GET /api/stations` - Get all MRT-3 stations
- `GET /api/stations/{id}` - Get station by ID

### Predictions
- `POST /api/predictions/generate` - Generate passenger density prediction
- `GET /api/predictions/history` - Get user's prediction history

### Saved Stations
- `GET /api/saved-stations` - Get user's saved stations
- `POST /api/saved-stations` - Save a station
- `DELETE /api/saved-stations/{id}` - Remove saved station

### Reports
- `POST /api/reports` - Submit an issue report
- `GET /api/reports` - Get user's submitted reports

## Prototype
View the Figma prototype: [TrainTrack Prototype](https://www.figma.com/proto/VIqFEhaQCSqQfeV23OPrec/Protoype-Figma---ITS120-L?node-id=1-17&t=WYXNk7q0mj4kwa0r-1&starting-point-node-id=1%3A17&scaling=contain&content-scaling=fixed)

### Key Pages
- Sign-in Page
- Email Verification Page
- Login Page
- Forgot Password Page
- Prediction Page (with Power BI integration)
- User Dashboard Page
- Report Page

## Scope
- **Coverage**: MRT-3 Line (13 stations)
- **Prediction Window**: 15-30 minutes ahead
- **Prediction Levels**: Light, Moderate, Heavy, Very Heavy
- **Future Expansion**: Architecture supports LRT-1 and LRT-2 integration

## Development Team
- Bahian, Eric Vincent
- Mauricio, Lord Aaron Isaac
- Pangan, Nela Ryne
- Ramirez, Phia Vhianna Reign
- Rupisan, Anthony James

**Course**: Application Development and Emerging Technologies (ITS120L-FOPM02)  
**Institution**: Mapua University, School of Information Technology  
**Instructor**: Prof. Crizepvill F. Dumalaog

## License
This project is developed as an academic requirement for ITS120 & ITS120L at Mapúa University Makati.

## Contact
For questions or issues, please submit through the Report Problem page or contact the development team.