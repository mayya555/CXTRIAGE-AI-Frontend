from .views import RegisterDoctorView, AiChatView

urlpatterns = [
    path('register/doctor/', RegisterDoctorView.as_view(), name='register_doctor'),
    path('chat/', AiChatView.as_view(), name='ai_chat'),
]

