# 🚀 Shiftly - Automatic Multi-Business Schedule Generator

## 🎯 Product Vision (Jan 31, 2026)
**For jewelry/retail managers in Barcelona [who]**, generates optimal 1‑month schedules [what], considering positions/seniority/languages/managers + employee preferences [how], **scalable multi‑tenant** (3–50 employees/business) [why].

## 👥 Roles and Permissions
| Role | Key Responsibilities |
|------|-----------------------|
| **SUPERADMIN** | Create ADMINS + companies |
| **ADMIN** | Company config, full CRUD employees, shifts |
| **MANAGER** | Generate/edit schedules, employee preferences |
| **EMPLOYEE** | View calendars, request holidays |

## 🏗️ Tech Stack
- **Backend**: Spring Boot **4.0.2** + Java 21 + Spring Security JWT + JPA/**MySQL** + **SpringDoc Swagger**
- **Frontend**: Angular 18 + Angular Material + JWT Interceptor
- **Architecture**: **Modular Monolith DDD** (bounded context packages: `company`, `employees`, `shifts`, `schedules`)

## 📊 Data Model (MySQL)
```
Company(id, name, opening="10:00", closing="21:00")
User(id, username, role, company_id)
Employee(id, name, position="SELLER|MANAGER|ADMIN", senior=boolean, languages[], prefsDays[], prefsShift, maxWeeklyHours=40, user_id)
Shift(id, company_id, type, start, end, totalCount=4, minEnglish=2, minChinese=1, minManagers=1)
Schedule(id, date, shift_id, employee_id)
HolidayRequest(id, employee_id, date, status=PENDING)
```

## 🔌 API Endpoints (Swagger `/swagger-ui.html`)
| Role | Endpoint | Method | Description |
|------|----------|--------|-------------|
| SUPERADMIN | `/api/superadmin/admins` | POST | Create ADMIN + company |
| All | `/api/auth/login` | POST | JWT token |
| ADMIN | `/api/companies/{id}/config` | GET/PUT | Company profile + shifts |
| ADMIN/MANAGER | `/api/companies/{id}/employees` | GET | List |
| ADMIN/MANAGER | `/api/employees/{id}` | GET | Detail |
| ADMIN | `/api/employees/{id}` | PUT/DELETE | Full CRUD |
| MANAGER | `/api/employees/{id}/prefs` | PUT | Schedule preferences |
| ADMIN/MANAGER | `/api/companies/{id}/schedules/generate?month=2026-02` | POST | Generate 1‑month |
| ADMIN/MANAGER | `/api/schedules/{id}` | PUT | Manual changes |
| EMPLOYEE | `/api/companies/{id}/global-calendar` | GET | All shifts |
| EMPLOYEE | `/api/employees/me/profile` | GET/PUT | Profile + photo |
| EMPLOYEE | `/api/employees/me/schedules` | GET | My schedules |

## ⚙️ Installation
```bash
# Backend
mvn spring-boot:run
# Swagger: localhost:8080/swagger-ui.html

# Frontend
ng serve -o
```

