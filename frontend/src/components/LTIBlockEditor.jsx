import React, { useState, useEffect } from 'react';

const LTIBlockEditor = () => {
  const [blockData, setBlockData] = useState({
    title: '',
    ltiConfig: '',
    customParams: '',
    gradingRule: 'none',
  });

  const [ltiConfigurations, setLtiConfigurations] = useState({});
  const [isLoading, setIsLoading] = useState(true);
  const [selectedLessonId, setSelectedLessonId] = useState('');
  const [availableLessons, setAvailableLessons] = useState([]);

  const gradingRules = [
    { id: 'none', name: 'Не оценивать' },
    { id: 'pass_fail', name: 'Зачёт/Незачёт' },
    { id: 'points', name: 'Балльная система' }
  ];

  useEffect(() => {
    const fetchConfigs = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/lti-config');
        if (!response.ok) {
          throw new Error('Failed to fetch LTI configurations');
        }
        const data = await response.json();
        
        setLtiConfigurations(data);
        setAvailableLessons(Object.keys(data));  
        
        if (Object.keys(data).length > 0) {
          setSelectedLessonId(Object.keys(data)[0]);
          setBlockData(prev => ({
            ...prev,
            title: data[Object.keys(data)[0]].toolName || '',
            ltiConfig: Object.keys(data)[0]
          }));
        }
      } catch (error) {
        console.error('Error fetching LTI configs:', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchConfigs();
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setBlockData(prev => ({ ...prev, [name]: value }));
  };

  const handleLessonChange = (e) => {
    const lessonId = e.target.value;
    setSelectedLessonId(lessonId);
    
    if (ltiConfigurations[lessonId]) {
      setBlockData(prev => ({
        ...prev,
        title: ltiConfigurations[lessonId].toolName || '',
        ltiConfig: lessonId
      }));
    }
  };

  const handleSave = async () => {
    if (!selectedLessonId) {
      alert('Пожалуйста, выберите урок');
      return;
    }

    const config = ltiConfigurations[selectedLessonId];
    if (!config) {
      alert('Выбранная конфигурация не найдена');
      return;
    }

    try {
      const endpoint = config.version === '1.3' 
        ? `http://localhost:8080/api/lti-config/${selectedLessonId}/v1.3`
        : `http://localhost:8080/api/lti-config/${selectedLessonId}/v1.1`;

      const updatedConfig = {
        ...config,
        customParams: blockData.customParams,
        gradingRule: blockData.gradingRule,
        toolName: blockData.title
      };

      const response = await fetch(endpoint, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(updatedConfig),
      });

      if (!response.ok) {
        throw new Error('Ошибка при обновлении конфигурации');
      }

      alert('Конфигурация успешно обновлена!');
    } catch (error) {
      console.error('Ошибка:', error);
      alert('Произошла ошибка при сохранении: ' + error.message);
    }
  };

  if (isLoading) {
    return <div>Загрузка конфигураций...</div>;
  }

  return (
    <div className="container">
      <div className="preview">
        <div className="title">{blockData.title || 'Название блока LTI'}</div>
        <div className="notice">
          В Редакторе курсов недоступно отображение внешнего вида инструментов LTI
        </div>
      </div>

      <div className="inspector">
        <div className="inspectorTitle">Настройки LTI-блока</div>

        <div className="formGroup">
          <div className="label">Выберите урок:</div>
          <select
            value={selectedLessonId}
            onChange={handleLessonChange}
            className="select"
          >
            {availableLessons.map(lessonId => (
              <option key={lessonId} value={lessonId}>
                {lessonId} (LTI {ltiConfigurations[lessonId]?.version})
              </option>
            ))}
          </select>
        </div>

        <div className="formGroup">
          <div className="label">Название блока:</div>
          <input
            type="text"
            name="title"
            value={blockData.title}
            onChange={handleChange}
            className="input"
            placeholder="Введите название блока"
          />
        </div>

        <div className="formGroup">
          <div className="label">Дополнительные параметры:</div>
          <textarea
            name="customParams"
            value={blockData.customParams}
            onChange={handleChange}
            className="textarea"
            placeholder="Введите параметры в формате: param1=value1&param2=value2"
          />
        </div>

        <div className="formGroup">
          <div className="label">Правило оценивания:</div>
          <select
            name="gradingRule"
            value={blockData.gradingRule}
            onChange={handleChange}
            className="select"
          >
            {gradingRules.map(rule => (
              <option key={rule.id} value={rule.id}>
                {rule.name}
              </option>
            ))}
          </select>
        </div>

        <button onClick={handleSave} className="saveButton">
          Сохранить блок
        </button>
      </div>
    </div>
  );
};

export default LTIBlockEditor;