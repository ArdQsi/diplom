import { useState } from 'react';
import LessonEditor from './components/LessonEditor';
import LtiConfigForm from './components/LtiConfigForm';
import CoursePreview from './components/CoursePreview';


const App = () => {
  const [activeTab, setActiveTab] = useState('editor');

  return (
    <div className="app-container">
      <div className="tabs">
        <button
          className={activeTab === 'editor' ? 'active' : ''}
          onClick={() => setActiveTab('editor')}
        >
          Редактор курса
        </button>
        <button
          className={activeTab === 'config' ? 'active' : ''}
          onClick={() => setActiveTab('config')}
        >
          Конфигурация LTI
        </button>
        <button
          className={activeTab === 'preview' ? 'active' : ''}
          onClick={() => setActiveTab('preview')}
        >
          Просмотр курса
        </button>
      </div>

      <>
        {activeTab === 'editor' && <LessonEditor />}
        {activeTab === 'config' && <LtiConfigForm />}
        {activeTab === 'preview' && <CoursePreview />}
      </>
    </div>
  );
}











// function App() {
//   const [config, setConfig] = useState(null);
//   const [viewMode, setViewMode] = useState('config'); // 'config' or 'view'
//   const [courseId, setCourseId] = useState('course123');
//   const [blockId, setBlockId] = useState('block456');

//   // Mock default config (in real app would fetch from API)
//   const defaultConfig = {
//     version: '1.3',
//     launchUrl: 'https://lti.toolprovider.com/launch',
//     deploymentId: 'newdex-default-deployment',
//     useDefaultCredentials: true
//   };

//   const handleSave = async (newConfig) => {
//     console.log('Saving config:', newConfig);
//     // In real app: POST to backend API
//     setConfig(newConfig);
//     setViewMode('view');
//   };

//   return (
//     <div className="container">
//       <h1>LTI Tool Configuration</h1>

//       {viewMode === 'config' ? (
//         <LtiConfigForm 
//           courseId={courseId}
//           blockId={blockId}
//           defaultConfig={defaultConfig}
//           onSave={handleSave}
//           onCancel={() => setViewMode('view')}
//         />
//       ) : (
//         <>
//           <button onClick={() => setViewMode('config')}>Edit Configuration</button>
//           {config && (
//             <LtiBlockView 
//               launchUrl={config.launchUrl}
//               ltiParams={{
//                 lti_version: config.version,
//                 lti_message_type: 'basic-lti-launch-request',
//                 resource_link_id: blockId,
//                 // Add other LTI params here
//               }}
//             />
//           )}
//         </>
//       )}
//     </div>
//   );
// }

export default App;
