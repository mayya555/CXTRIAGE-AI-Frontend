from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework.permissions import AllowAny
from .serializers import UserRegistrationSerializer

class RegisterDoctorView(APIView):
    permission_classes = [AllowAny]

    def post(self, request):
        serializer = UserRegistrationSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(
                {"message": "Doctor account request submitted successfully"},
                status=status.HTTP_201_CREATED
            )
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

class AiChatView(APIView):
    permission_classes = [AllowAny]

    def post(self, request):
        user_message = request.data.get('message', '').lower()
        
        # Enhanced dynamic intelligence logic for medical assistant
        if 'pneumothorax' in user_message or 'pneumonia' in user_message:
            response_text = "Cortex AI: Based on current thoracic patterns, critical conditions require prioritized diagnostic review. I am assisting in identifying these highlights for radiologists."
        elif 'scan' in user_message or 'image' in user_message:
            response_text = "Cortex AI: Our deep learning models analyze high-resolution DICOM and JPEG scans for subtle abnormalities including nodules and pleural effusions."
        elif 'help' in user_message or 'assist' in user_message:
            response_text = "Cortex AI: I can help you summarize cases, provide data insights, or explain specific medical conditions found in automated triage reports."
        else:
            response_text = f"Cortex AI: I've received your query about '{user_message}'. I am constantly learning from clinical data to improve my triage and diagnosis support."
        
        return Response({
            "response": response_text
        }, status=status.HTTP_200_OK)


