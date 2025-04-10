import { useEffect, useState } from "react";

const CoursePreview = () => {
    const [isLoading, setIsLoading] = useState(true);
    const courseId = "lesson1"

    useEffect(() => {
      const iframe = document.getElementById('lti-iframe');
      
      const fetchLtiLaunch = async () => {
        try {
          const response = await fetch(`http://localhost:8080/api/courses/${courseId}/launch`);
          const html = await response.text();
          
          if (iframe && iframe.contentWindow) { 
            iframe.contentWindow.document.open();
            iframe.contentWindow.document.write(html);
            iframe.contentWindow.document.close();
          }
        } catch (error) {
          console.error('LTI launch failed:', error);
        } finally {
          setIsLoading(false);
        }
      };
  
      fetchLtiLaunch();
    }, [courseId]);
  
    return (
      <div style={{ height: '800px', width: '100%' }}>
        {isLoading && <div>Loading LTI tool...</div>}
        <iframe 
          id="lti-iframe"
          title="LTI Launch Frame"
          style={{ border: 'none', width: '100%', height: '100%' }}
        />
      </div>
    );
};

export default CoursePreview;