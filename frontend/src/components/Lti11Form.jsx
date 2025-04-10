import React, { useState } from 'react';

const Lti11Form = () => {
  const [courseId, setCourseId] = useState('');
  const [formData, setFormData] = useState({
    version: '1.1',
    toolName: '',
    toolDescription: '',
    toolUrl: '',
    consumerKey: '',
    sharedSecret: '',
    resourceLinkId: ''
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitStatus, setSubmitStatus] = useState(null);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleCourseIdChange = (e) => {
    setCourseId(e.target.value);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!courseId) {
      alert('Please enter Course ID');
      return;
    }

    setIsSubmitting(true);
    setSubmitStatus(null);

    try {
      const response = await fetch(`http://localhost:8080/api/lti-config/v1.1?courseId=${courseId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData),
      });

      if (!response.ok) {
        throw new Error('Network response was not ok');
      }

      setSubmitStatus('success');
      alert('Configuration saved successfully!');
    } catch (error) {
      console.error('Error:', error);
      setSubmitStatus('error');
      alert('Error saving configuration');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="form-section">
      <h3>LTI 1.1 Configuration</h3>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Tool Name *</label>
          <input
            type="text"
            name="toolName"
            value={formData.toolName}
            onChange={handleChange}
            required
            placeholder="Name that teachers will recognise"
          />
        </div>

        <div className="form-group">
          <label>Tool Description</label>
          <textarea
            name="toolDescription"
            value={formData.toolDescription}
            onChange={handleChange}
            placeholder="Optional description to help identify the tool"
            rows="3"
          />
        </div>

        <div className="form-group">
          <label>Course ID *</label>
          <input
            type="text"
            value={courseId}
            onChange={handleCourseIdChange}
            required
            placeholder="Enter courseId in the format lessonN"
          />
        </div>

        <div className="form-group">
          <label>Tool URL *</label>
          <input
            type="url"
            name="toolUrl"
            value={formData.toolUrl}
            onChange={handleChange}
            required
            placeholder="http://localhost:8080/launch"
          />
          <p className="help-text">Base URL of tool</p>
        </div>

        <div className="form-group">
          <label>Consumer Key *</label>
          <input
            type="text"
            name="consumerKey"
            value={formData.consumerKey}
            onChange={handleChange}
            required
            placeholder="Paste consumer key"
          />
        </div>

        <div className="form-group">
          <label>Shared Secret *</label>
          <input
            type="text"
            name="sharedSecret"
            value={formData.sharedSecret}
            onChange={handleChange}
            required
            placeholder="Paste consumer secret"
          />
        </div>

        <div className="form-group">
          <label>Resource Link ID</label>
          <input
            type="text"
            name="resourceLinkId"
            value={formData.resourceLinkId}
            onChange={handleChange}
            placeholder="Enter resource link ID"
          />
        </div>

        <div className="form-actions">
          <button
            type="submit"
            className="save-btn"
            disabled={isSubmitting}
          >
            {isSubmitting ? 'Saving...' : 'Save Configuration'}
          </button>
        </div>


        {submitStatus === 'success' && (
          <p className="success-message">Configuration saved successfully!</p>
        )}
        {submitStatus === 'error' && (
          <p className="error-message">Failed to save configuration</p>
        )}
      </form>
    </div>
  );
};

export default Lti11Form;